package com.DocumentQ.ASystem.service;

import com.DocumentQ.ASystem.congifuration.RabbitMQConfig;
import com.DocumentQ.ASystem.dto.DocumentProcessingMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendForProcessing(Long documentId, String url) {

        DocumentProcessingMessage message =
                new DocumentProcessingMessage(documentId,url);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DOCUMENT_EXCHANGE,
                RabbitMQConfig.PROCESSING_ROUTING_KEY,
                message
        );
    }
}
