package com.DocumentQ.ASystem.service.preprocessing;


import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentExtractor {

    @Autowired
    private Tika tika;


    public String extractText(MultipartFile file)
    {
        String fileContent = null;
        try {
            fileContent = tika.parseToString(file.getInputStream());
        }catch(Exception e)
        {
            System.out.println("error on tika par" + e);
        }

        return fileContent;
    }
}
