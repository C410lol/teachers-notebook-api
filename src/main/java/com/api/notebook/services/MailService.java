package com.api.notebook.services;

import com.api.notebook.enums.EmailStatus;
import com.api.notebook.models.entities.EmailEntity;
import com.api.notebook.utils.repositories.MailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailRepository mailRepository;
    private final JavaMailSender javaMailSender;

    public void save(EmailEntity emailEntity) {
        mailRepository.save(emailEntity);
    }

    public void sendEmail(EmailEntity emailEntity) {
        try {
            var simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(emailEntity.getTo());
            simpleMailMessage.setSubject(emailEntity.getSubject());
            simpleMailMessage.setText(emailEntity.getText());
            javaMailSender.send(simpleMailMessage);
            emailEntity.setStatus(EmailStatus.SENT);
        } catch (Exception exception) {
            emailEntity.setStatus(EmailStatus.ERROR);
        } finally {
            save(emailEntity);
        }
    }

}
