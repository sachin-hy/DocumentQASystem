package com.DocumentQ.ASystem.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

//@Entity
//@Table(name = "document_embeddings")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class DocumentEmbeddings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentDetails document;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT")
    private String content;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}