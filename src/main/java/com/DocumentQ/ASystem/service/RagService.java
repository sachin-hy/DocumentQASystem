package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;

import com.DocumentQ.ASystem.service.preprocessing.DocumentChunk;
import com.DocumentQ.ASystem.service.preprocessing.DocumentDataCleaner;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;





@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    @Autowired
    private ChatClient chatClient;



    @Autowired
    private DocumentDetailsRepository documentRepository;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DocumentChunk documentChunk;

    @Autowired
    private DocumentDataCleaner  documentDataCleaner;


    @Transactional
    public void indexDocument(Long documentId, String text) throws IOException, TikaException {

        log.info("Starting indexing for document id: {}", documentId);

        DocumentDetails documentDetails = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        text = documentDataCleaner.clean(text);

        // create chunk of the  dcouments
        List<String> chunks = documentChunk.createChunks(text);

        log.info("Total chunks created: {}", chunks.size());


        // creating documents of the chunks recived
        List<Document> documents = chunks.stream()
                .map(content -> new Document(
                        content,
                        Map.of(
                                "document_id", documentDetails.getId().toString(),
                                "file_name", documentDetails.getFileName(),
                                "chunk_type", "chunk"
                        )))
                .collect(Collectors.toList());


        log.info("Saving {} chunks into Vector Store.", documents.size());

        // store the documents in the database ,
        // again make there embedding to store
        vectorStore.add(documents);

        log.info("Document indexed successfully.");
    }



    public String queryDocument(String query, String email, Long documentId) {

        log.info("Searching document id {} for query: {}", documentId, query);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(5)
                .similarityThreshold(0.40)
                .filterExpression("document_id == '" + documentId + "'")
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        // if document simalarity search is empty then extract teh summary of the
        // document and try providing answer using the summary
        if (documents.isEmpty()) {

            log.info("No similar chunks found. Searching summary.");

            searchRequest = SearchRequest.builder()
                    .filterExpression("chunk_type == 'summary'")
                    .build();

            // get the summary from the database
            documents = vectorStore.similaritySearch(searchRequest);
        }

        // join the documents text into single text
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String conversationId = email + ":" + documentId;

        return generateResponse(query, conversationId, context);
    }


    public String generateSummary(String fileContent)
            throws IOException, TikaException {

        fileContent = fileContent.replaceAll("\\s+", " ").trim();

        log.info("Generating summary.");

        return chatClient.prompt()
                .system("""
                    You are a professional document assistant summarizer.
                    Summarize the given document.
                    Keep the summary concise.
                    """)
                .user(fileContent)
                .call()
                .content();
    }


    // generate message for user using the content
    public String generateResponse(String query,String conversationId, String context)
    {

        String system = """
          You are a strict document-based Question Answering (QA) assistant.

          Your role:
          - Answer questions using ONLY the information present in the provided context fetch from vector db using similarity search.
          - The context may be irrelevant, incomplete, or unrelated to the question.
          - Take information from context and return answer using the context in user friendly way.
          - Remove all markdown formatting symbols (such as **, *, _, backticks),
                    remove special characters except basic punctuation,
                    and return the text as clean, readable plain English.
                    Do not change the meaning of the content.
         
          Answering rules:
          - Be concise and factual.
          - Quote relevant lines from the context when answering.
          - If even unsure, choose NO ANSWER FOUND.
        """;


        String user = """
         Context of the document from vector db using similarity search:
          -------------------------
          %s
          -------------------------

          User Question:
          %s

          Task:
          - First, determine whether the context is directly relevant to the question.
          - Only if the answer is present in the context, provide the answer using the context only.
          - If the context does NOT explicitly answer the question, respond EXACTLY with:
            "The answer is not found in the provided document."
          
         """.formatted(context, query);


        log.info("system and user prompt are set");

        String response = chatClient
               .prompt()
               .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
               .system(system)
               .user(user)
               .call()
               .content();

       log.info("response returned : {}", response);
       return response;
    }
}
