package com.vidasalud.auditoria.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "citas.exchange";
    public static final String QUEUE = "auditoria.cita.queue";
    public static final String ROUTING_KEY = "cita.*";

    @Bean
    public TopicExchange citasExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue auditoriaQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding auditoriaBinding(Queue auditoriaQueue, TopicExchange citasExchange) {
        return BindingBuilder.bind(auditoriaQueue).to(citasExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
