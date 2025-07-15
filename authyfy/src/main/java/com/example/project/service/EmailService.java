package com.example.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;


    public void sendEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome, " + name);
        message.setText("Hi " + name + ",\n\nWelcome to our platform! We're glad to have you.");
        message.setFrom(fromEmail);

        mailSender.send(message);
    }
    
    public void sendResetOtpEmail(String toEmail,String otp) {
    	 SimpleMailMessage message = new SimpleMailMessage();
         message.setTo(toEmail);
         message.setSubject("Password Reset Otp");
         message.setText("Your OTP for resetting your password is "+otp+". user this Otp to proceed");
         message.setFrom(fromEmail);
         
         mailSender.send(message);
    }
    
    public void sendOtpEmail(String toEmail,String otp) {
   	 SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password verification Otp");
        message.setText("Your OTP  is "+otp+". Verify your account using OTP");
        message.setFrom(fromEmail);
        
        mailSender.send(message);
   }
}


