package com.DocumentQ.ASystem.runners;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;



@Component
public class LLMRunner implements ApplicationRunner {


    @Autowired
    private ChatClient chatClient;



    @Override
    public void run(ApplicationArguments args) throws Exception {

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
