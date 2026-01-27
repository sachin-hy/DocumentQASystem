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
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
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

    @Autowired
    private DocumentProcessor documentProcessor;

    @Autowired
    private DocumentExtractor documentExtractor;

    @Autowired
    private DocumentChunk  documentChunk;

    @Autowired
    private EmbeddingModel  embeddingModel;



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

            //extract the text
            String text = documentExtractor.extractText(file);

            ragService.indexDocument(document.getId(),text);

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


    @PostMapping("/testingurl")
    public ResponseEntity<List<String>> checkUrl(@RequestParam  String url) {

//        try{
//            String data = documentProcessor.processURL(url);
//
//            return ResponseEntity.ok(data);
//        }catch(Exception e){
//            log.error("Error processing URL", e);
//        }

      //  List<String> list = documentChunk.breakText(url);

      //  float[] arr = embeddingModel.embed(list.getFirst());
     //   for (float f : arr) {
     //       System.out.println(f);
     //   }

        List<String> list = documentChunk.createChunks(url);
        System.out.println("size of chunks list = > " + list.size());
        return new ResponseEntity<>(list, HttpStatus.OK);

    }

    @PostMapping("/url")
    public ResponseEntity<UploadResponse> uploadUrl(@RequestParam("url") String url,
                                            Principal principal)
    {
        try {
            log.info("Recieved upload request for url: {}", url);
            log.info("request from user : {}", principal.getName());

            String data = documentProcessor.processURL(url);

            Users user = usersService.findByEmail(principal.getName());
            DocumentDetails documentDetails = documentService.save(data,user);
            log.info("document details saved successfully");

            ragService.indexDocument(documentDetails.getId(),data);

            log.info("Document uploaded and indexed successfully");
            UploadResponse response = UploadResponse.builder()
                    .documentId(documentDetails.getId())
                    .fileName(documentDetails.getFileName())
                    .message("Document uploaded and indexed successfully")
                    .build();

            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e)
        {
            UploadResponse response = UploadResponse.builder()
                    .message("Error processing URL")
                    .build();
            log.error("Error processing URL",e);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

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
