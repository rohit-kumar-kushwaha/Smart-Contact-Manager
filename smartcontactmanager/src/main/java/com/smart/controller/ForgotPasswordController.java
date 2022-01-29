package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

@Controller
public class ForgotPasswordController {

	Random random = new Random(100000);

	@Autowired
	private EmailService emailService;

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
			
//		generating otp of 4 digits
			
			
		int otp = random.nextInt(999999);
		System.out.println("OTP :"+otp);
		String subject = "OPT From Smart Contact Manager";
		String message = "<h1> OTP = " +otp+ "</h1>";
		String to = email;
			
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			
			session.setAttribute("otp", otp);
			return "verify_otp";
		}
		else {
			
			session.setAttribute("message", "Check your email id !!");
			return "forgot_email_form";
		}
			
		
	}
}
