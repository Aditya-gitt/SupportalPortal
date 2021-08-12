package com.example.demo.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailService {

    private Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("kulkarniaditya32@gmail.com", "Cooladitya@gmail_1");
            }
        });
        return session;
    }

    public void sendEmail(String email, String message, String subject) throws MessagingException {
        Session session = this.getSession();
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress("kulkarniaditya32@gmail.com", false));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("email"));
        msg.setSubject(subject);
        msg.setText(message);
        msg.setSentDate(new Date());

        Transport.send(msg);
    }

}
