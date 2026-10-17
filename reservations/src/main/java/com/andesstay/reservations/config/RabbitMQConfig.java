package com.andesstay.reservations.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${andesstay.rabbitmq.exchanges.direct}")
    private String directExchangeName;

    @Value("${andesstay.rabbitmq.exchanges.dlx}")
    private String dlxExchangeName;

    @Value("${andesstay.rabbitmq.queues.email}")
    private String emailQueueName;

    @Value("${andesstay.rabbitmq.queues.email-dlq}")
    private String emailDlqName;

    @Value("${andesstay.rabbitmq.queues.housekeeping}")
    private String housekeepingQueueName;

    @Value("${andesstay.rabbitmq.queues.housekeeping-dlq}")
    private String housekeepingDlqName;

    @Value("${andesstay.rabbitmq.queues.voucher}")
    private String voucherQueueName;

    @Value("${andesstay.rabbitmq.queues.voucher-dlq}")
    private String voucherDlqName;

    @Value("${andesstay.rabbitmq.routing-keys.email-send}")
    private String emailRoutingKey;

    @Value("${andesstay.rabbitmq.routing-keys.housekeeping-ticket}")
    private String housekeepingRoutingKey;

    @Value("${andesstay.rabbitmq.routing-keys.voucher-gen}")
    private String voucherRoutingKey;

    @Value("${andesstay.rabbitmq.routing-keys.dlq}")
    private String dlqRoutingKey;

    // 1. Exchanges (Directo para comandos y DLX para mensajes con error)
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // 2. Colas Principales con Dead Letter Exchange vinculado
    @Bean
    public Queue emailQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return new Queue(emailQueueName, true, false, false, args);
    }

    @Bean
    public Queue emailDlq() {
        return new Queue(emailDlqName, true);
    }

    @Bean
    public Queue housekeepingQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return new Queue(housekeepingQueueName, true, false, false, args);
    }

    @Bean
    public Queue housekeepingDlq() {
        return new Queue(housekeepingDlqName, true);
    }

    @Bean
    public Queue voucherQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return new Queue(voucherQueueName, true, false, false, args);
    }

    @Bean
    public Queue voucherDlq() {
        return new Queue(voucherDlqName, true);
    }

    // 3. Bindings (Unir cada cola a su exchange con su routing key)
    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with(emailRoutingKey);
    }

    @Bean
    public Binding emailDlqBinding(Queue emailDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(emailDlq).to(deadLetterExchange).with(dlqRoutingKey);
    }

    @Bean
    public Binding housekeepingBinding(Queue housekeepingQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(directExchange).with(housekeepingRoutingKey);
    }

    @Bean
    public Binding housekeepingDlqBinding(Queue housekeepingDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(housekeepingDlq).to(deadLetterExchange).with(dlqRoutingKey);
    }

    @Bean
    public Binding voucherBinding(Queue voucherQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(voucherQueue).to(directExchange).with(voucherRoutingKey);
    }

    @Bean
    public Binding voucherDlqBinding(Queue voucherDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(voucherDlq).to(deadLetterExchange).with(dlqRoutingKey);
    }

    // 4. Conversor JSON para serializar los mensajes
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}