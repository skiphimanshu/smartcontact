package com.smart.controller;

import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepo;
import com.smart.entites.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepo userRepo;
	@Autowired
	private EmailService emailService;

	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - SCM");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - SCM");
		return "about";
	}

	// Register

	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - SCM");
		m.addAttribute("user", new User());
		return "signup";
	}

	// save user
	@RequestMapping(value = "/doRegister", method = RequestMethod.POST)
	public String doRegister(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {

		try {

			if (!agreement) {
				System.out.println("Please Check Term and condition");

				throw new Exception("Please Check Term and condition");

			}

			if (result.hasErrors()) {
				m.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageurl("default.png");

			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

			System.out.println("agreement " + agreement);
			System.out.println("user" + user);

			User u1 = this.userRepo.getUserByUserName(user.getEmail());
			if (u1 == null) {   


				// Genetrate otp of 4 digit
				int otp = ThreadLocalRandom.current().nextInt(1000, 9999);

				// write code to send otp email
				String subject = "OTP from SCM";
				String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px;'>" + "<h1>" + "OTP is "
						+ "<b>" + otp + "</n>" + "</h1>" + "</div>";
				String to = user.getEmail();
				// send OTP code
				boolean res = this.emailService.sendEmail(message, subject, to);
				if (res) {
					session.setAttribute("myotp", otp);
					session.setAttribute("email", user.getEmail());
					session.setAttribute("user",user);
					return "reg_otp_verify";
				} else {
					session.setAttribute("message", new Message("Something went wromg", "alert-danger"));
					return "signup";
				}
				
				// End of OTP send code
			} else {
				session.setAttribute("message", new Message("User Already Exist with this emal id plz Login", "alert-warning"));
				return "login";
			}

		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong", "alert-danger"));

			return "signup";
		}

	}

	// Verify otp

	@PostMapping("/verify-RegOTP")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession httpSession, Model m) {

		m.addAttribute("title", "Verify-Otp SCM");

		Integer myotp = (Integer) httpSession.getAttribute("myotp");
		String email = (String) httpSession.getAttribute("email");
		User user = (User) httpSession.getAttribute("user");
		System.out.println(myotp);
		System.out.println(email);
		System.out.println(otp);

		// match two otp
		if (myotp.equals(otp)) {

			// After matching OTP, User Save into db
			
			User UserRes = this.userRepo.save(user);
			m.addAttribute("user", new User());
			httpSession.setAttribute("message", new Message("Suceesfully Registerd Please Login", "alert-success"));
			
			return "login";

		} else {
			System.out.println("Otp Doesnot match");
			httpSession.setAttribute("message", new Message("Invalid OTP", "alert-danger"));
			return "verifyotp";
		}
	}

	// login
	@RequestMapping(value = "/signin")
	public String login(Model m) {

		m.addAttribute("title", "Login - SCM");
		return "login";
	}
}