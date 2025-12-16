package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.dto.ChatMessageDto;
import com.DocumentQ.ASystem.entity.ChatMessage;
import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.repository.ChatMessageRepository;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {


    @Autowired
    private RagService ragService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private DocumentDetailsRepository documentRepository;


    @Transactional
    public ChatMessage saveUserMessage(Long documentId, String query, String message) {

        try {
            DocumentDetails document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            ChatMessage userMessage = ChatMessage.builder()
                    .document(document)
                    .role("USER")
                    .query(query)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .build();

            document.getChatMessageList().add(userMessage);
            documentRepository.save(document);

            return userMessage;
        }catch(Exception e)
        {
            log.error("error while saving chat details : {}" , e);
            return null;
        }
    }


    public ChatMessageDto processQuery(Long documentId,String email, String query) {

        log.info("Processing query for document {}: {}", documentId, query);



        String response = ragService.queryDocument(query, email, documentId);


        log.info("Response of Query : {} ", response );
        ChatMessage userMessage = saveUserMessage(documentId, query, response);

        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .id(userMessage.getId())
                .query(userMessage.getQuery())
                .message(userMessage.getMessage())
                .createdAt(userMessage.getCreatedAt())
                .build();

        return chatMessageDto;
    }
}
