package com.badasstechie.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${event-bus.exchange-name}")
    private String exchangeName;

    @Value("${event-bus.queues.update-stock}")
    private String updateStockQueueName;

    @Value("${event-bus.queues.order-awaiting-payment}")
    private String orderAwaitingPaymentQueueName;

    @Value("${event-bus.queues.order-payment-processed}")
    private String orderPaymentProcessedQueueName;

    @Bean
    public Queue updateStockQueue() {
        return new Queue(updateStockQueueName);
    }

    @Bean
    public Queue orderAwaitingPaymentQueue() {
        return new Queue(orderAwaitingPaymentQueueName);
    }

    @Bean
    public Queue orderPaymentProcessedQueue() {
        return new Queue(orderPaymentProcessedQueueName);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding updateStockBinding(Queue updateStockQueue, TopicExchange exchange) {
        return BindingBuilder.bind(updateStockQueue).to(exchange).with(updateStockQueueName);
    }

    @Bean
    public Binding orderAwaitingPaymentBinding(Queue orderAwaitingPaymentQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderAwaitingPaymentQueue).to(exchange).with(orderAwaitingPaymentQueueName);
    }

    @Bean
    public Binding orderPaymentProcessedBinding(Queue orderPaymentProcessedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPaymentProcessedQueue).to(exchange).with(orderPaymentProcessedQueueName);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean AmqpTemplate updateStockTemplate(ConnectionFactory connectionFactory) {
        return createTemplate(connectionFactory, updateStockQueueName);
    }

    @Bean AmqpTemplate orderAwaitingPaymentTemplate(ConnectionFactory connectionFactory) {
        return createTemplate(connectionFactory, orderAwaitingPaymentQueueName);
    }

    @Bean AmqpTemplate orderPaymentProcessedTemplate(ConnectionFactory connectionFactory) {
        return createTemplate(connectionFactory, orderPaymentProcessedQueueName);
    }

    private AmqpTemplate createTemplate(ConnectionFactory connectionFactory, String routingKey) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(exchangeName);
        template.setRoutingKey(routingKey);
        template.setMessageConverter(messageConverter());
        return template;
    }
}