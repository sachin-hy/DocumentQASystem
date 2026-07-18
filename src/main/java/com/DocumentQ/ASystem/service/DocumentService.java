package com.DocumentQ.ASystem.service;

import com.DocumentQ.ASystem.dto.ChatMessageDto;
import com.DocumentQ.ASystem.dto.DocumentChatResponseDto;
import com.DocumentQ.ASystem.dto.DocumentResponseDto;
import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.entity.Users;
import com.DocumentQ.ASystem.repository.DocumentDetailsRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Set;
import java.util.UUID;



@Service
@Slf4j
public class DocumentService {

    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private DocumentDetailsRepository documentRepository;


    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;

    @Transactional
    public DocumentDetails uploadDocument(MultipartFile file, Users user) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a valid file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 25 MB.");
        }

        String contentType = file.getContentType();

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Only PDF and TXT files are allowed."
            );
        }





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

        log.info("Document uploaded successfully. Document Id: {}", document.getId());

        return document;
    }





    @Transactional
    public List<DocumentResponseDto> getDocumentsByUser(Users user) {

        log.info("Fetching documents for user: {}", user.getEmail());

        List<DocumentDetails> documents = documentRepository.findByUserDetails(user);

        return documents.stream()
                .map(document -> new DocumentResponseDto(
                        document.getId(),
                        document.getFileName(),
                        document.getFileSize(),
                        document.getCreatedAt()))
                .toList();
    }



    public DocumentChatResponseDto findById(Long documentId) {

        log.info("Fetching document details for id: {}", documentId);

        DocumentDetails document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Document not found with id: " + documentId));

        List<ChatMessageDto> messages = document.getChatMessageList()
                .stream()
                .map(chat -> new ChatMessageDto(
                        chat.getId(),
                        chat.getQuery(),
                        chat.getMessage(),
                        chat.getCreatedAt()))
                .toList();

        return DocumentChatResponseDto.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .message(messages)
                .build();
    }



    @Transactional
    public DocumentDetails save(String data, Users user) {

        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Document content cannot be empty.");
        }

        log.info("Saving URL document.");

        DocumentDetails document = DocumentDetails.builder()
                .userDetails(user)
                .fileName(UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                .uniqueFileName(UUID.randomUUID().toString())
                .contentType("Text")
                .fileSize((long) data.length())
                .storagePath("URL")
                .indexed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .chatMessageList(new ArrayList<>())
                .build();

        documentRepository.save(document);

        log.info("URL document saved successfully. Document Id: {}", document.getId());



        return document;
    }
}