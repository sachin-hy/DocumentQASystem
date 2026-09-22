package com.DocumentQ.ASystem.dto;

public class DocumentProcessingMessage {

    private Long documentId;
    private String url;

    public DocumentProcessingMessage() {
    }

    public DocumentProcessingMessage(Long documentId, String url) {
        this.documentId = documentId;
        this.url = url;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
