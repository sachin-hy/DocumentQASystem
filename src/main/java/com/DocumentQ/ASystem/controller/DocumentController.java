package com.DocumentQ.ASystem.controller;

import com.DocumentQ.ASystem.dto.DocumentChatResponseDto;
import com.DocumentQ.ASystem.dto.DocumentResponseDto;
import com.DocumentQ.ASystem.dto.UploadRequestDto;
import com.DocumentQ.ASystem.dto.UploadResponse;
import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.entity.Users;
import com.DocumentQ.ASystem.service.DocumentService;
import com.DocumentQ.ASystem.service.RagService;
import com.DocumentQ.ASystem.service.UsersService;
import com.DocumentQ.ASystem.service.preprocessing.DocumentChunk;
import com.DocumentQ.ASystem.service.preprocessing.DocumentExtractor;
import com.DocumentQ.ASystem.service.preprocessing.DocumentProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final UsersService usersService;





    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(
            @ModelAttribute @Valid UploadRequestDto uploadRequestDto,
            Principal principal) throws Exception {

        MultipartFile file = uploadRequestDto.getFile();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        log.info("Received upload request for file: {}", file.getOriginalFilename());

        Users user = usersService.findByEmail(principal.getName());

        DocumentDetails document = documentService.uploadDocument(file, user);


        log.info("Document {} uploaded successfully and queued for processing.", document.getId());

        UploadResponse response = UploadResponse.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .message("Document uploaded successfully and queued for processing.")
                .build();

        return ResponseEntity.ok(response);
    }







    @PostMapping("/url")
    public ResponseEntity<UploadResponse> uploadUrl(
            @RequestParam String url,
            Principal principal) throws Exception {

        log.info("Received URL upload request: {}", url);

        DocumentDetails document = documentService.save(url,principal.getName());


        log.info("URL indexed successfully. Document Id={}", document.getId());

        UploadResponse response = UploadResponse.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .message("URL indexed successfully.")
                .build();

        return ResponseEntity.ok(response);
    }








    @GetMapping
    public ResponseEntity<List<DocumentResponseDto>> getDocuments(
            Principal principal) {

        Users user = usersService.findByEmail(principal.getName());

        List<DocumentResponseDto> documents =
                documentService.getDocumentsByUser(user);

        log.info("Returning {} documents.", documents.size());

        return ResponseEntity.ok(documents);
    }








    @GetMapping("/document")
    public ResponseEntity<DocumentChatResponseDto> getDocumentChatDetails(
            @RequestParam Long documentId) {

        DocumentChatResponseDto response =
                documentService.findById(documentId);

        log.info("Returning document details for id={}", documentId);

        return ResponseEntity.ok(response);
    }

}