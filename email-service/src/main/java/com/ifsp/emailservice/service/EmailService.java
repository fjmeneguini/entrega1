package com.ifsp.emailservice.service;

import com.ifsp.emailservice.dto.EmailRecordDto;
import com.ifsp.emailservice.model.EmailModel;
import com.ifsp.emailservice.model.EmailStatus;
import com.ifsp.emailservice.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final EmailRepository emailRepository;
    private final JavaMailSender mailSender;
    private final String emailFrom;

    public EmailService(EmailRepository emailRepository,
                        JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String emailFrom) {
        this.emailRepository = emailRepository;
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
    }

    public EmailModel sendEmail(EmailRecordDto emailRecordDto) {
        EmailModel emailModel = new EmailModel();
        emailModel.setUserId(emailRecordDto.getUserId());
        emailModel.setEmailFrom(emailFrom);
        emailModel.setEmailTo(emailRecordDto.getEmailTo());
        emailModel.setSubject(emailRecordDto.getSubject());
        emailModel.setText(emailRecordDto.getText());
        emailModel.setSendDateEmail(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(emailRecordDto.getEmailTo());
            message.setSubject(emailRecordDto.getSubject());
            message.setText(emailRecordDto.getText());
            mailSender.send(message);
            emailModel.setStatus(EmailStatus.SENT);
        } catch (Exception ex) {
            emailModel.setStatus(EmailStatus.ERROR);
        }

        return emailRepository.save(emailModel);
    }
}
