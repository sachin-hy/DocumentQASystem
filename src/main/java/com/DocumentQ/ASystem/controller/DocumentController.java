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
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/documents")
@Slf4j
public class DocumentController {


    @Autowired
    private DocumentService documentService;

    @Autowired
    private RagService ragService;

    @Autowired
    private UsersService usersService;


    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(@ModelAttribute UploadRequestDto uploadRequestDto,
                                                         Principal principal ) throws TikaException, IOException {
        try {
            MultipartFile file = uploadRequestDto.getFile();
            log.info("Received upload request for file: {}", file.getOriginalFilename());

            String email = principal.getName();

            Users user = usersService.findByEmail(email);
            // Upload document
            DocumentDetails document = documentService.uploadDocument(file,user);

            ragService.indexDocument(document.getId(),file);

            UploadResponse response = UploadResponse.builder()
                    .documentId(document.getId())
                    .fileName(document.getFileName())
                    .message("Document uploaded and indexed successfully")
                    .build();

            return ResponseEntity.ok(response);
        }catch(Exception e)
        {

            log.error("Error uploading document", e);
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message("Error uploading document: " + e.getMessage())
                            .build());

        }
    }


    @GetMapping
    public ResponseEntity<?> getDocuments(Principal principal){

        String email = principal.getName();

        Users user = usersService.findByEmail(email);

        List<DocumentResponseDto> list = documentService.getDocumentsByUser(user);

        log.info("total document returned {}" , list.size());

        return new ResponseEntity<>(list, HttpStatus.OK);

    }

    @GetMapping("/document")
    public ResponseEntity<?> getDocumentChatMessageDetails(
                                                @RequestParam("documentId") String documentId)
    {

         Long docId = Long.parseLong(documentId);

         DocumentChatResponseDto documentChatResponseDto = documentService.findById(docId);

         log.info("document chat detail returnerd ");
         return new ResponseEntity<>(documentChatResponseDto, HttpStatus.OK);
    }



}
