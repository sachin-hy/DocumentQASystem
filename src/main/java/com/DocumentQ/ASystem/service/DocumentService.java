package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.dto.ChatMessageDto;
import com.DocumentQ.ASystem.dto.DocumentChatResponseDto;
import com.DocumentQ.ASystem.dto.DocumentResponseDto;
import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.entity.Users;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

@Service
@Slf4j
public class DocumentService {


    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private Tika tika;

    @Autowired
    private DocumentDetailsRepository documentRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private  EmbeddingModel embeddingModel;


    @Transactional
    public DocumentDetails uploadDocument(MultipartFile file, Users user) throws IOException, TikaException {

        log.info("Uploading document: {}", file.getOriginalFilename());

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);


        Files.write(filePath, file.getBytes());

        DocumentDetails document = DocumentDetails.builder()
                .userDetails(user)
                .fileName(file.getOriginalFilename())
                .uniqueFileName(uniqueFileName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(filePath.toString())
                .indexed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .chatMessageList(new ArrayList<>())
                .build();

        documentRepository.save(document);

        return document;


    }


    @Transactional
    public List<DocumentResponseDto> getDocumentsByUser(Users user) {

        List<DocumentDetails> result = documentRepository.findByUserDetails(user);

        List<DocumentResponseDto> documentResponseDtos = result.stream().map( (doc) -> new DocumentResponseDto(doc.getId(),doc.getFileName(),doc.getFileSize(),doc.getCreatedAt())).toList();
        return documentResponseDtos;
    }

    public DocumentChatResponseDto findById(Long docId) {

        DocumentDetails doc = documentRepository.findById(docId).orElse(null);

        List<ChatMessageDto> chatMessageDto = doc.getChatMessageList().stream().map((chat) -> new ChatMessageDto(chat.getId(),chat.getQuery(),chat.getMessage(),chat.getCreatedAt())).toList();

        DocumentChatResponseDto documentChatResponseDto = DocumentChatResponseDto.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .message(chatMessageDto)
                .build();

        return documentChatResponseDto;
    }

    public DocumentDetails save(String data, Users user) {


        DocumentDetails documentDetails = DocumentDetails.builder()
                .userDetails(user)
                .fileName(UUID.randomUUID().toString().replace("-","").substring(0,10))
                .uniqueFileName(UUID.randomUUID().toString())
                .contentType("Text")
                .fileSize((long) data.length())
                .storagePath("random")
                .indexed(false)
                .createdAt(LocalDateTime.now())
                .chatMessageList(new ArrayList<>())
                .build();

        documentRepository.save(documentDetails);

        return documentDetails;
    }
}
