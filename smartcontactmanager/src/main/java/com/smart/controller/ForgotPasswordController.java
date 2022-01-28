package com.smart.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {
	
	Random random = new Random(100000);
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	
	//email id form open handler
		@PostMapping("/send-otp")
		public String sendOTP(@RequestParam("email") String email) {
			
			System.out.println("EMAIL : "+email);
			
//			generating otp of 4 digits
			
			
			int otp = random.nextInt(999999);
			System.out.println("OTP :"+otp);
			
			
			return "verify_otp";
		}
}
