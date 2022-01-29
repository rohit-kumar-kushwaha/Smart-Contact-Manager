package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotPasswordController {

	Random random = new Random(100000);

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm(Model model) {
		
		model.addAttribute("title", "Forgot password");
		return "forgot_email_form";
	}

	// send otp handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session, Model model) {
			
		System.out.println("EMAIL : "+email);
		
		model.addAttribute("title", "OTP");
		
		User user = this.userRepository.getUserByUserName(email);
		
		if(user != null) {
			
//			generating otp of 4 digits
			
			
			int otp = random.nextInt(999999);
			System.out.println("OTP :"+otp);
			String subject = "OPT From Smart Contact Manager";
			String message = "<h1> OTP = " +otp+ "</h1>";
			String to = email;
				
			boolean flag = this.emailService.sendEmail(subject, message, to);
			
			if(flag) {
				
				session.setAttribute("myotp", otp);
				session.setAttribute("email", email);
				session.setAttribute("otpMessage", "we have send OTP to your email...");
				return "verify_otp";
			}
			else {
				
				session.setAttribute("message", "Check your email id !!");
				return "forgot_email_form";
			}
			
		}
		else {
			session.setAttribute("message", "Email is not exist !!");
			return "forgot_email_form";
		}
			
//		
			
		
	}
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		
		int myotp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(myotp == otp) {
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message", "You have entered wrong OTP !!");
			return "verify_otp";
		}
		
	}
	
	// change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		
		session.setAttribute("message", "You have entered wrong OTP !!");
		return "redirect:/signin?change=Password changed successfully...";
		
		
		
	}
}
