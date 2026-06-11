package com.ifsp.emailservice.consumer;

import com.ifsp.emailservice.dto.EmailRecordDto;
import com.ifsp.emailservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void receive(EmailRecordDto emailRecordDto) {
        emailService.sendEmail(emailRecordDto);
    }
}
