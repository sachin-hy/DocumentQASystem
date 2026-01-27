package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;

import com.DocumentQ.ASystem.service.preprocessing.DocumentChunk;
import com.DocumentQ.ASystem.service.preprocessing.DocumentDataCleaner;

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
import java.io.IOException;
import java.util.List;
import java.util.Map;
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



    public void indexDocument(Long documentId, String text) throws TikaException, IOException {

           log.info("Starting indexing for document ID: {}", documentId);

           DocumentDetails documentDetails = documentRepository.findById(documentId)
                   .orElseThrow(() -> new RuntimeException("Document not found"));

           text = documentDataCleaner.clean(text);




         List<String> chunks  = documentChunk.createChunks(text);

           log.info("total number of chunks created = " + chunks.size());

           /*
              get the summary of the document
            */
          String summary = "Summary : " + generateSummary(text);


           List<Document> documents = chunks.stream()
                   .map((content) ->
                           {
                               return new Document(content,
                                       Map.of("document_id", documentDetails.getId().toString(), "file_name", documentDetails.getFileName(),"chunk_type","chunk"));
                           }
                   ).collect(Collectors.toList());

          documents.add(new Document(summary,Map.of("document_id",documentDetails.getId().toString(),"file_name",documentDetails.getFileName(),"chunk_type","summary")));

            /*
            *
            * store it in vector db
            * */

            log.info("storing data in vector db");
            vectorStore.add(documents);
            log.info("after storing data in vector db");


    }




    // retrive datafrom database
    public String queryDocument(String query,String email, Long documentId) {

        try {
            log.info("Starting query for query : {}", query);
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(5)
                    .similarityThreshold(0.40)
                    .filterExpression("document_id == '" + documentId + "'")
                    .build();

            List<Document> list = vectorStore.similaritySearch(searchRequest);


            // if nothing is found using similarity search then
            // provide the summary of the document
            if(list.isEmpty())
            {
                searchRequest = SearchRequest.builder().
                        filterExpression("chunk_type == 'summary'")
                        .build();
                list = vectorStore.similaritySearch(searchRequest);
            }


            log.info("Found total {} similar documents", list.size());
            String dbResult = list.stream().map(Document::getText).collect(Collectors.joining("\\n\\n"));

            log.info("Generating user Friendly response ");

            String id = email + ":" + documentId.toString();


            log.info("df result = " + dbResult);
            return generateResponse(query,id, dbResult);


        }catch(Exception e)
        {
            System.out.println("Error in queryDocument method = " + e);
            return null;
        }
    }




    public String generateSummary(String fileContent) throws IOException, TikaException {

        fileContent = fileContent.replaceAll("\\s+", " ").trim();

        String systemInstructions = """
            You are a professional document assistant summarizer.
            Summarize the provided text concisely for a document QA System.
            Max length  of summary is 50000 words
            """;
        log.info("generating summaary for the context of length = " + fileContent.length());
        return chatClient.prompt()
                .system(systemInstructions)
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

      //  String system = "You are a helpful assistant. Use ONLY the provided context to answer. If the answer isn't there, say: 'The answer is not found in the provided document.'";

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
