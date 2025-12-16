package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

//    @Autowired
//    private ChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private Tika tika;

    @Autowired
    private DocumentDetailsRepository documentRepository;

    @Autowired
    private VectorStore vectorStore;


    public void indexDocument(Long documentId, MultipartFile file) throws TikaException, IOException {

        log.info("Starting indexing for document ID: {}", documentId);

        DocumentDetails documentDetails = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        List<String> chunks = createChunks(file);

        List<Document> documents = chunks.stream()
                .map((content) -> new Document(content,
                        Map.of("document_id", documentDetails.getId().toString(),"file_name",documentDetails.getFileName()))
                ).toList();

        vectorStore.add(documents);


    }




    // get the chunk of the file content
    public List<String> createChunks(MultipartFile file) throws IOException, TikaException {

        String fileContent = tika.parseToString(file.getInputStream());


        Long fileSize = file.getSize();


        fileContent = fileContent.replaceAll("\\s+", " ").trim();

        int minChunkSize = 1000;
        int maxChunks = 1000;

        List<String> chunks = new ArrayList<>();
        int totalLength = fileContent.length();

        int chunkSize = minChunkSize;
        int estimatedChunks = (int) Math.ceil((double) totalLength / chunkSize);

        if(estimatedChunks > maxChunks){
            chunkSize = (int) Math.ceil((double) totalLength / maxChunks);
        }

        int start = 0;

        while (start < totalLength && chunks.size() < maxChunks) {
            int end = Math.min(start + chunkSize, totalLength);

            if (end < totalLength) {
                int lastPeriod = fileContent.lastIndexOf('.', end);
                if (lastPeriod > start) {
                    end = lastPeriod + 1;
                }
            }

            String chunk = fileContent.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end;
        }

        return chunks;



    }

    // retrive datafrom database
    public String queryDocument(String query,String email, Long documentId) {

        try {
            log.info("Starting query for query : {}", query);
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(5)
                    .similarityThreshold(0.75)
                    .filterExpression("document_id == '" + documentId + "'")
                    .build();

            List<Document> list = vectorStore.similaritySearch(searchRequest);

            log.info("Found total {} similar documents", list.size());
            String dbResult = list.stream().map(Document::getFormattedContent).collect(Collectors.joining("\\n\\n"));

            log.info("Generating user Friendly response ");

            String id = email + ":" + documentId.toString();
            String response = generateResponse(query,id, dbResult);

            return response;
        }catch(Exception e)
        {
            System.out.println("Error in queryDocument method = " + e);
            return null;
        }
    }


    // generate message for user using the content
    public String generateResponse(String query,String conversationId, String context)
    {
        String system = """
         You are a document assistant that answers questions based ONLY on provided content.

         Instructions:
         1. Answer using ONLY the context provided below
         2. Do NOT use external knowledge or training data
         3. Quote relevant passages when supporting your answer
         4. If the answer is not in the context or context is empty, respond: "The answer is not found in the provided document."
         5. Be concise and factual
         6. Avoid speculation or assumptions

         Guidelines:
         - Stick to document facts only
         - Cite specific sections when relevant
         - If partially answerable, state what is covered and what isn't
         """;


        String user = """
         Context from the document:
         ---
         %s
         ---

         User Question: %s

         Based ONLY on the context above, please answer the question. If information is unavailable, say so explicitly.
         """.formatted(context, query);

//        Message userMessage = new UserMessage(user);
//        Message systemMessage = new SystemMessage(system);
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


//        ChatResponse chatResponse = chatModel.call( new Prompt(
//                List.of(systemMessage, userMessage),
//                ChatOptions.builder().temperature(0.0).build()
//        ));
//
//        log.info("query response modified as user friendly");
//
//        String response =chatResponse.getResult().getOutput().getText();


    }
}
