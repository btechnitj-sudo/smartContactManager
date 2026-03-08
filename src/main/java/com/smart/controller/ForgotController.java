package com.smart.controller;

import java.security.SecureRandom;

import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;




@Controller
public class ForgotController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    // Random random = new Random(1000);

     private static final SecureRandom random = new SecureRandom();

    ForgotController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/forgot")
    public String openEmailForm(){


        return "forgot-email-form";
    }

@PostMapping("/send-otp")
public String sendOTP(@RequestParam("email") String email, 
                     RedirectAttributes redirectAttributes, 
                     HttpSession session) {
    
    // CHECK IF USER EXISTS FIRST
    User user = this.userRepository.getUserByUserName(email);
    
    if (user == null) {
        redirectAttributes.addFlashAttribute(
            "message", 
            new Message("User does not exist with this email!", "alert-danger")
        );
        return "redirect:/forgot";  // or "forgot-email-form"
    }
    
    // User exists - now generate and send OTP
    int otp = 100000 + random.nextInt(900000);
    System.out.println("OTP " + otp);
    
    String subject = "OTP From SCM";
    String message = "<div style='border:1px solid #e2e2e2;padding:20px'>"
                   + "OTP is <b>" + otp + "</b>"
                   + "</div>";
    
    boolean flag = this.emailService.sendEmail(email, subject, message);
    
    if (flag) {
        session.setAttribute("resetOtp", otp);
        session.setAttribute("resetEmail", email);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + 1000 * 60 * 5);
        
        redirectAttributes.addFlashAttribute(
            "message",
            new Message("OTP sent successfully to your email!", "alert-success")
        );
        return "redirect:/verify-otp";
    } else {
        redirectAttributes.addFlashAttribute(
            "message",
            new Message("Failed to send OTP. Check your email id!", "alert-danger")
        );
        return "redirect:/forgot";
    }
}

// show OTP form
@GetMapping("/verify-otp")
public String showVerifyOtpForm() {
    return "verify-otp";   // your Thymeleaf/JSP view name
}

@PostMapping("/verify-otp")
public String verifyOtp(@RequestParam("otp") int otp, HttpSession session,RedirectAttributes redirectAttributes) {

    int myOtp = (int) session.getAttribute("resetOtp");
    String email = (String) session.getAttribute("resetEmail");

    if (myOtp==otp) {

      User user = this.userRepository.getUserByUserName(email);

        if (user == null) {
            // send error message and go back to forgot page
            redirectAttributes.addFlashAttribute(
                    "message",
                    new Message("User does not exist!!", "alert-danger")
            );
            return "redirect:/forgot";        // mapping for your forgot-email page
        } else {
            // user exists – show change password form
            return "password-change-form";
        }

    } else {
        // OTP is wrong -> show verify-otp page again with message
        redirectAttributes.addFlashAttribute(
                "message",
                new Message("Wrong OTP,Please enter again!", "alert-danger")
        );
        return "redirect:/verify-otp";
    }
}

//change password

@PostMapping("/change-password")
public String changePassword(@RequestParam("newpassword")String newpassword,HttpSession session,RedirectAttributes redirectAttributes){
     String email = (String) session.getAttribute("resetEmail");
     User user=this.userRepository.getUserByUserName(email);
     user.setPassword(this.passwordEncoder.encode(newpassword));
     this.userRepository.save(user);

       redirectAttributes.addFlashAttribute(
                "message",
                new Message("Password changed successfully", "alert-danger")
        );
 return  "redirect:/signin";

}
}