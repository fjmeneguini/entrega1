package com.ifsp.userservice.producer;

import com.ifsp.userservice.dto.EmailDto;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserProducer {

    private final AmqpTemplate amqpTemplate;
    private final String queueName;

    public UserProducer(AmqpTemplate amqpTemplate, @Value("${broker.queue.email.name:default.email}") String queueName) {
        this.amqpTemplate = amqpTemplate;
        this.queueName = queueName;
    }

    public void sendEmail(EmailDto emailDto) {
        amqpTemplate.convertAndSend(queueName, emailDto);
    }
}