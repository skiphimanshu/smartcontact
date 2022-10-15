package com.smart.controller;

import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepo;
import com.smart.entites.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@RequestMapping("/forgot")
	public String forgotPass(Model m, HttpSession session) {
		m.addAttribute("title", "Forgot Password");
		return "forgotpass";
	}

	// send otp handler

	@PostMapping("/send-otp")
	public String processOtp(@RequestParam("email") String email, HttpSession httpSession,Model m) {
		System.out.println("email :" + email);
		
		m.addAttribute("title","Forgot Password");
		
		// Genetrate otp of 4 digit
		int otp = ThreadLocalRandom.current().nextInt(1000, 9999);

		// write code to send otp email
		String subject = "OTP from SCM";
		String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px;'>" + "<h1>" + "OTP is " + "<b>" + otp
				+ "</n>" + "</h1>" + "</div>";
		String to = email;

		User user = this.userRepo.getUserByUserName(email);

		if (user == null) {
			// user is not available in database
			httpSession.setAttribute("message",
					new Message("User doesnot exist with this email id", "alert-success"));
			return "forgotpass";

		} else {
			// user is availbale in the data base
			boolean res = this.emailService.sendEmail(message, subject, to);
			if (res) {
				httpSession.setAttribute("myotp", otp);
				httpSession.setAttribute("email", email);
				return "verifyotp";
			} else {
				httpSession.setAttribute("message", new Message("Something went wromg", "alert-danger"));
				return "forgotpass";
			}
		}

	}

	// verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession httpSession, Model m) {
		
		m.addAttribute("title", "Verify Otp");

		Integer myotp = (Integer) httpSession.getAttribute("myotp");
		String email = (String) httpSession.getAttribute("email");
		System.out.println(myotp);
		System.out.println(email);
		System.out.println(otp);

		// match two otp
		if (myotp.equals(otp)) {

			System.out.println("Otp  match");

			return "resetpassword";

		} else {
			System.out.println("Otp Doesnot match");
			httpSession.setAttribute("message", new Message("Invalid OTP", "alert-danger"));
			return "verifyotp";
		}
	}
	
	// reset password hanlder
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam("newPassword") String password,HttpSession httpSession) {
		
		String email = (String) httpSession.getAttribute("email");
		User user = this.userRepo.getUserByUserName(email);
		user.setPassword(bCryptPasswordEncoder.encode(password));
		user.setAgreement(true);
		this.userRepo.save(user);
		
		return "redirect:/signin?change=Password change Successfully...";
		
	}
	
	
}
