package com.DocumentQ.ASystem.service.preprocessing;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class DocumentExtractor {

    @Autowired
    private Tika tika;

    public String extractText(MultipartFile file)  throws IOException, TikaException {


        log.info("Extracting text from file: {}", file.getOriginalFilename());
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String text = tika.parseToString(file.getInputStream());

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Unable to extract text from the document.");
        }

        log.info("Text extracted successfully.");

        return text;
    }
}