package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.EmailStatus;
import com.inventory.inventorymanagementsystem.entity.Email;
import com.inventory.inventorymanagementsystem.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    public void sendCredentialsEmail(String toEmail, String username, String password, String roleName) {
        String subject = "Your Account Credentials for Inventory Management System";
        String body = String.format("""
                Dear %s,

                Your account for the Inventory Management System has been created successfully as a %s.

                
                Login Email: %s
                Password: %s

                

                Regards,
                Inventory Management System Team
                """, username, roleName, toEmail, password);


        Email emailLog = Email.builder()
                .sender("shreya.amanaganti@coditas.com")
                .recipient(toEmail)
                .subject(subject)
                .body(body)
                .status(EmailStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Email savedEmail = emailRepository.save(emailLog);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            savedEmail.setStatus(EmailStatus.SENT);
        } catch (Exception e) {
            savedEmail.setStatus(EmailStatus.FAILED);
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace(); // Optional: logs full stack trace
        }

        savedEmail.setUpdatedAt(LocalDateTime.now());
        emailRepository.save(savedEmail);
    }
}

