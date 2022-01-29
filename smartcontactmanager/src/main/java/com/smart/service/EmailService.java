package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {

		boolean flag = false;
		String host = "smtp.gmail.com";

//		get the system property
		Properties properties = System.getProperties();

		String from = "rohit.pumca@gmail.com";

//		setting important information to properties object

//		host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

//		get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("rohit.pumca@gmail.com", "@3888Rohit");
			}

		});

		session.setDebug(true);

//		step 2 : compose the mail
		MimeMessage mimeMessage = new MimeMessage(session);

		try {

//			from email
			mimeMessage.setFrom(from);

//			adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

//			adding subject to message
			mimeMessage.setSubject(subject);

//			adding text to message
			//mimeMessage.setText(message);
			mimeMessage.setContent(message, "text/html");

//			step 3 : send
//			send the message using Transport class
			Transport.send(mimeMessage);

			System.out.println("send successfully.......");

			flag = true;

		} catch (MessagingException e) {

			e.printStackTrace();
		}
		return flag;

	}

}
