package com.kulacolin.auth.service;

import com.kulacolin.auth.model.User;
import com.kulacolin.auth.util.EmailUtil;
import com.kulacolin.auth.dao.UserDAO;

import jakarta.mail.MessagingException;

import java.security.SecureRandom;

public class EmailService {
    private final UserDAO userDAO;

    public EmailService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void sendEmail(User user, String token, String type) {  
        String emailContent = "";
        if (type.equals("email")) {
            emailContent = "Your email verification code is: " + token;
        } else if (type.equals("2FA")) {
            emailContent = "Your 2FA verification code is: " + token;
        } else if (type.equals("resetPass")) {
            emailContent = "Your password verification code is: " + token;
        }
    
        try {
            // send email with generated token
            EmailUtil.sendEmail(user.getEmail(), "Email Verification", emailContent);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }

}
