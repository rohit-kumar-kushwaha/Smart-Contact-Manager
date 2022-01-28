package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// executing for all handler automatically
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		System.out.println("USERNAME : " + userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER " + user);

		model.addAttribute(user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			model.addAttribute("title", "Add Contact");

			// processing and uploading file
			if (!file.isEmpty()) {
				// upload the file to folder and update the name of contact
				String picName = contact.getCid() + "_" + file.getOriginalFilename();
				contact.setImgUrl(picName);
				File saveFile = new ClassPathResource("static/img/contact").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + picName);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("contact image uploade");

			}
			contact.setUser(user);

			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("DATA : " + contact);

			System.out.println("Added to database");

			// message success......
			session.setAttribute("message", new Message("Your contact is added !! Add more", "success"));

		} catch (Exception e) {
			System.out.println("ERROR : " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try again...", "danger"));
		}

		return "normal/add_contact_form";
	}

	// show contact handler
	// per page 5
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show User Contact");

		
		String userName = principal.getName(); User user =
		this.userRepository.getUserByUserName(userName);
		  
		// send all user list
		  
		//List<Contact> contacts = user.getContacts();
		
		Pageable pageable = PageRequest.of(page, 6);
		 
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		  
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}
	
	// showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("Cid : "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
			
		}
		
		return "normal/contact_detail";
	}
	
	// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model,
			Principal principal, HttpSession session) {
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		System.out.println("DELETE : "+user.getId()+" "+contact.getUser().getId());
		try {
		if(user.getId() == contact.getUser().getId()) {
//			contact.setUser(null);
//			this.contactRepository.delete(contact);
			if(contact.getImgUrl() != null) {
				File deleteFile = new ClassPathResource("static/img/contact").getFile();
				File file1 = new File(deleteFile, contact.getImgUrl());
				file1.delete();
				System.out.println("if deleted");
				
			}
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact deleted succesfully...", "success"));
		}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	// open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Model model, HttpSession session, Principal principal) {
		try {
			// old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty()) {
				
				if(oldContactDetail.getImgUrl() != null) {
					// delete old photo
					File deleteFile = new ClassPathResource("static/img/contact").getFile();
					System.out.println("File name : "+oldContactDetail.getImgUrl()+"  : "+file);
					File file1 = new File(deleteFile, oldContactDetail.getImgUrl());
					file1.delete();
				}
				
				
				// update new photo
				String picName = contact.getCid() + "_" + file.getOriginalFilename();
				File saveFile = new ClassPathResource("static/img/contact").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + picName);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImgUrl(picName);
				
				
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			System.out.println("CONTACT NAME : "+contact.getName());
			System.out.println("CONTACT ID : "+contact.getCid ());
			
			session.setAttribute("message", new Message("Your contact is updated...", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	// Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model){
		
		model.addAttribute("title", "Profile page");
		return "normal/profile";
	}
	
	// open update profile handler
		@PostMapping("/update-profile/{id}")
		public String updateProfile(@PathVariable("id") Integer id, Model model) {
			
			model.addAttribute("title", "Update Profile");
//			
//			Contact contact = this.contactRepository.findById(cid).get();
//			model.addAttribute("contact", contact);
			
			return "normal/update_profile";
		}
		
		// update profile handler
		@PostMapping("/process-update-profile")
		public String updateProfileHandler(@ModelAttribute User user, @RequestParam("profileImage") MultipartFile file, 
				Model model, HttpSession session) {
			try {
				//User user = this.userRepository.getUserByUserName(principal.getName());
				
				if(!file.isEmpty()) {
					
					if(user.getImgUrl() != null) {
						// delete old photo
						File deleteFile = new ClassPathResource("static/img/user").getFile();
						//System.out.println("File name : "+oldContactDetail.getImgUrl()+"  : "+file);
						File file1 = new File(deleteFile, user.getImgUrl());
						file1.delete();
					}
						
					// update new photo
					File saveFile = new ClassPathResource("static/img/user").getFile();
					String picName = user.getId() +"_"+ file.getOriginalFilename();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator +picName);

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					user.setImgUrl(picName);
					
					
				}
				
				//User user = this.userRepository.getUserByUserName(principal.getName());
				this.userRepository.save(user);
				//this.contactRepository.save(contact);
				System.out.println("USER NAME : "+user.getName());
				System.out.println("USER ID : "+user.getId ());
				
				session.setAttribute("message", new Message("Your profile is updated...", "success"));
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			
			return "redirect:/user/profile";
		}
		
		// open setting handler
		@GetMapping("/settings")
		public String openSetting(Model model) {
			
			model.addAttribute("title", "Change Password");
			
			return "normal/settings";
		}
		
		// change password handler
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("oldPassword") String oldPassword, 
				@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
			
			System.out.println("OLD PASSWORD : "+oldPassword);
			System.out.println("NEW PASSWORD : "+newPassword);
			
			User currentUser = this.userRepository.getUserByUserName(principal.getName());
			
			System.out.println("CURRENT PASSWORD : "+currentUser.getPassword());
			
			if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
				
				if(newPassword.length() >= 6) {
					currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
					this.userRepository.save(currentUser);
					session.setAttribute("message", new Message("Your password is successfully changed...", "success"));
				}
				else {
					session.setAttribute("message", new Message("New Password must be minimum 6 characters !!", "danger"));
					return "redirect:/user/settings";
				}
				
				
			}
			else {
				session.setAttribute("message", new Message("Please enter currect old password !!", "danger"));
				return "redirect:/user/settings"; 
			}
			
			
			return "redirect:/user/index"; 
		}

}
