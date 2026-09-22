package com.DocumentQ.ASystem.utils;

import com.DocumentQ.ASystem.exception.FileUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class FileUploader {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.upload-folder:document-qa-system}")
    private String uploadFolder;

    public record UploadResult(String url, String publicId) {}

    public UploadResult uploadFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            log.warn("Attempted to upload an empty file.");
            throw new IllegalArgumentException("File must not be empty");
        }


        Map<?, ?> uploadResult;

        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", uploadFolder,
                    "overwrite", false,
                    "unique_filename", true
            ));

        } catch (IOException e) {
            log.error("Cloudinary upload failed for file '{}'", file.getOriginalFilename(), e);
            throw new FileUploadException("Internal Server Error! Try Again");
        }

        String url = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        log.info("File '{}' uploaded to Cloudinary. publicId={}", file.getOriginalFilename(), publicId);

        return new UploadResult(url, publicId);
    }


    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            log.info("Deleting file from Cloudinary. publicId={}", publicId);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary. publicId={}", publicId, e);
            throw new FileUploadException("Could not delete file. Try again.");
        }

    }
}