package com.smart.service;

public interface  EmailService {
    //  boolean sendEmail(String to, String subject, String message);

     //send email withHtml
    boolean sendEmail(String to,String subject,String htmlContent);

}
