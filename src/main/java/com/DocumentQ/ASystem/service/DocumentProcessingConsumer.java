package com.DocumentQ.ASystem.service;


import com.DocumentQ.ASystem.congifuration.RabbitMQConfig;
import com.DocumentQ.ASystem.dto.DocumentProcessingMessage;
import com.DocumentQ.ASystem.entity.DocumentDetails;
import com.DocumentQ.ASystem.service.preprocessing.DocumentChunk;
import com.DocumentQ.ASystem.service.preprocessing.DocumentDataCleaner;
import com.DocumentQ.ASystem.service.preprocessing.DocumentProcessor;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentProcessingConsumer {

    @Autowired
    private DocumentProcessor documentProcessor;

    @Autowired
    private DocumentDataCleaner documentDataCleaner;

    @Autowired
    private DocumentChunk documentChunk;

    @Autowired
    private RagService ragService;


    @RabbitListener(queues = RabbitMQConfig.PROCESSING_QUEUE)
    public void processDocument(DocumentProcessingMessage message) throws TikaException, IOException {

        String text = documentProcessor.processURL(message.getUrl());

        String cleanedText = documentDataCleaner.clean(text);

        List<String> chunkList = documentChunk.createChunks(cleanedText);

        ragService.indexDocument(message.getDocumentId(), chunkList);

    }

}

