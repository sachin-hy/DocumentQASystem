package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;




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


           String summary = "Summary : " + generateSummary(file);

           //add the summary to the chunks list
           chunks.add(summary);

           List<Document> documents = chunks.stream()
                   .map((content) -> new Document(content,
                           Map.of("document_id", documentDetails.getId().toString(), "file_name", documentDetails.getFileName()))
                   ).toList();
            vectorStore.add(documents);


    }




    // get the chunk of the file content
    public List<String> createChunks(MultipartFile file) throws IOException, TikaException {

         System.out.println("create chunks is called");
        String fileContent = null;
        try {
              fileContent = tika.parseToString(file.getInputStream());
         }catch(Exception e)
         {
             System.out.println("error on tika par" + e);
         }
       System.out.println("file content is stored in filecontent variable");

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
                    .similarityThreshold(0.40)
                    .filterExpression("document_id == '" + documentId + "'")
                    .build();

            List<Document> list = vectorStore.similaritySearch(searchRequest);


            if(list.size() == 0)
            {
                return "The answer is not found in the provided document.";
            }


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

    public String generateSummary(MultipartFile file) throws IOException, TikaException {

        String fileContent = tika.parseToString(file.getInputStream());

        fileContent = fileContent.replaceAll("\\s+", " ").trim();

        String system = """
                You are a professional document assistant summarizer .
                Summarize the given text for a document QA System application .
                The text is %s
                """.formatted(fileContent);

        String response = chatClient.prompt()
                .system(system)
                .call()
                .content();

        return response;

    }


    // generate message for user using the content
    public String generateResponse(String query,String conversationId, String context)
    {
        String system = """
          You are a strict document-based Question Answering (QA) assistant.

          Your role:
          - Answer questions using ONLY the information present in the provided context.
          - The context may be irrelevant, incomplete, or unrelated to the question.

          STRICT RULES (must follow):
          1. If the context does NOT clearly and explicitly contain the answer to the question, respond EXACTLY with:
          "The answer is not found in the provided document."
          2. Do NOT infer, guess, summarize unrelated content, or use general knowledge.
          3. Do NOT answer based on assumptions or partial matches.
          4. Only answer if the context directly addresses the user's question.
          5. If the context talks about a different topic than the question, treat it as NO ANSWER FOUND.
          6. If key terms from the question are missing in the context, treat it as NO ANSWER FOUND.

          Answering rules:
          - Be concise and factual.
          - Quote relevant lines from the context when answering.
          - If even slightly unsure, choose NO ANSWER FOUND.
        """;


        String user = """
         Context from the document:
          -------------------------
          %s
          -------------------------

          User Question:
          %s

          Task:
          - First, determine whether the context is directly relevant to the question.
          - If the context does NOT explicitly answer the question, respond EXACTLY with:
            "The answer is not found in the provided document."
          - Only if the answer is clearly present, provide the answer using the context only.
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
