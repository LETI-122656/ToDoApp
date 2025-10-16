package com.example.examplefeature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class Email {

    private final JavaMailSender mailSender;

    @Autowired
    public Email(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Envia um email simples de texto plano */
    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

}
