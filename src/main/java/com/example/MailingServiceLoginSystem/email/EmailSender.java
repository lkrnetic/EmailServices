package com.example.MailingServiceLoginSystem.email;

public interface EmailSender {
    void send(String to, String email, String subject, String sender);
}
