package com.DocumentQ.ASystem.congifuration;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;


@Configuration
public class RabbitMQConfig {



    public static final String PROCESSING_QUEUE = "document.processing.queue";

    public static final String DOCUMENT_EXCHANGE = "document.exchange";



    public static final String PROCESSING_ROUTING_KEY = "document.processing";




    /*document processing queue*/
    @Bean
    public Queue processingQueue() {

        return new Queue(
                PROCESSING_QUEUE,
                true
        );
    }

    /* document exchange */
    @Bean
    public DirectExchange documentExchange() {

        return new DirectExchange(
                DOCUMENT_EXCHANGE,
                true,
                false
        );
    }




    /* create a binding to the processing queue using processing routing key to document exchange (direct exchange)*/
    @Bean
    public Binding processingQueueBinding(
            Queue processingQueue,
            DirectExchange documentExchange) {

        return BindingBuilder
                .bind(processingQueue)
                .to(documentExchange)
                .with(PROCESSING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {

        RabbitTemplate rabbitTemplate =
                new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(jsonMessageConverter);

        return rabbitTemplate;
    }

}
