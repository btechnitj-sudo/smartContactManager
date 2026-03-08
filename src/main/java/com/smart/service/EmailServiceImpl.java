package com.smart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

      @Autowired
    private JavaMailSender mailSender;

    private Logger logger= LoggerFactory.getLogger(EmailServiceImpl.class);

    
    // @Override
    // public boolean sendEmail(String to, String subject, String message) {
    //     try {
    //         SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    //         simpleMailMessage.setTo(to);
    //         simpleMailMessage.setSubject(subject);
    //         // simpleMailMessage.setText(message);
    //         simpleMailMessage.setContent(message,"text/html");
    //         simpleMailMessage.setFrom("codewithnaveen15@gmail.com");

    //         mailSender.send(simpleMailMessage);
    //         logger.info("Email sent successfully to {}", to);
    //         return true;  // Success
    //     } catch (Exception e) {  // Changed from MessagingException to Exception
    //         logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
    //         return false;  // Failure
    //     }
    // }

     @Override
    public boolean sendEmail(String to, String subject, String htmlContent) {
        MimeMessage mimeMessage=mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent,true);
            helper.setFrom("codewithnaveen15@gmail.com");
            mailSender.send(mimeMessage);
          logger.info("Email sent successfully to {}", to);
            return true;  // Success
       } catch (Exception e) {  // Changed from MessagingException to Exception
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
             return false;  // Failure
        }


    }

}