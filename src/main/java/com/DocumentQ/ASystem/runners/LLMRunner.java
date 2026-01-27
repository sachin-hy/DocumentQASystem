package com.DocumentQ.ASystem.runners;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
public class LLMRunner implements ApplicationRunner {


    @Autowired
    private ChatClient chatClient;

   @Autowired
   private EmbeddingModel embeddingModel;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try{
            if(embeddingModel == null)
            {
                System.out.println("Embedding Model is Null");
            }


            Document document = new Document("Hello World");

            float[] em = embeddingModel.embed(document);

            System.out.println("Embedding Model embedded Successfully");

            for(int i =0; i < em.length; i++)
            {
                System.out.println(em[i] + " ");
            }

        }catch(Exception e)
        {
            System.err.println("embedding is NOT working: " + e.getMessage());
            e.printStackTrace();
        }


        try {

             if(chatClient == null)
             {
                 System.out.println("Chat Client is Null");
             }

             String response = chatClient
                     .prompt()
                                 .user("Hello how are youv")
                                     .call()
                                             .content();
            System.out.println("LLM is working. Sample response: " + response);
        } catch (Exception e) {
            System.err.println("LLM is NOT working: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
