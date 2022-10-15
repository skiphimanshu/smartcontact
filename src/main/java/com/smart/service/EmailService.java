package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	
	public boolean sendEmail(String message, String subject, String to) {
		// variable for gmail
		boolean f = false;
		String from = "skiphimanshu@gmail.com";
		String host = "smtp.gmail.com";
		// get the system properties
		Properties properties = System.getProperties();
		System.out.println("Properties " + properties);

		// setting important information to properties object
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// step -1 to get session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("skiphimanshu@gmail.com", "soquochpllosnndm");
			}

		});

		session.setDebug(true);
		// step-2 //compose message
		MimeMessage m = new MimeMessage(session);
		try {
			// from email
			m.setFrom(from);

			//
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			m.setSubject(subject);

			// adding text to mail
			/* m.setText(message); */
			m.setContent(message,"text/html");

			// send message
			Transport.send(m);
			System.out.println("sent successfully....");
			f = true;

		} catch (Exception e) {
			e.printStackTrace();

		}
		return f;
	}
}
