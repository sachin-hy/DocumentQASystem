package com.DocumentQ.ASystem.controller;


import com.DocumentQ.ASystem.dto.ChatMessageDto;
import com.DocumentQ.ASystem.dto.QueryRequest;
import com.DocumentQ.ASystem.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody QueryRequest request,
                                   Principal principal)
    {
        log.info("Received query for document {}: {}", request.getDocumentId(), request.getQuery());

        log.info("Received documentid {}", request.getDocumentId());

        Long dId = Long.parseLong(request.getDocumentId());

        String email = principal.getName();

        ChatMessageDto response = chatService.processQuery(dId, email, request.getQuery());
        log.info("chat response = {}", response);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
