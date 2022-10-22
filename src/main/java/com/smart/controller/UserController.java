package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepo;
import com.smart.dao.UserRepo;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private ContactRepo contactRepo;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String name = principal.getName();

		User user = this.userRepo.getUserByUserName(name);

		m.addAttribute("user", user);
	}

	// user dashboard

	@RequestMapping("/index")
	public String dashboard(Model m) {

		m.addAttribute("title", "Dashboard - SCM");

		return "normal/user_dashboard";
	}

	// user add contact

	@GetMapping("/add-contact")
	public String addcontact(Model m) {

		m.addAttribute("title", "AddContact - SCM");
		m.addAttribute("contact", new Contact());
		return "normal/user_addcontact";
	}

	// save contact to database
	@PostMapping("/process-contact")
	public String savecontact(@ModelAttribute Contact contact, @RequestParam("pimage") MultipartFile file,
			Principal principal, Model m, HttpSession httpSession) {

		try {

			if (file.isEmpty()) {

				contact.setImage("contact.png");

			} else {
				// save the file url into contact -> database
				String imgName = file.getOriginalFilename();

				contact.setImage(imgName);

				// save the file url into folder img
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + imgName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is upload");
				
			}

			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepo.save(user);
			System.out.println("data" + contact);
			httpSession.setAttribute("message", new Message("contact Added successfully", "alert-success"));

			return "redirect:/user/contacts/0";

		} catch (Exception e) {

			System.out.println(e.getMessage());
			m.addAttribute("contact", contact);
			httpSession.setAttribute("message", new Message("something went wrong", "alert-danger"));

			return "normal/user_addcontact";
		}
	}

	// fetching specify user contact page wise

	@GetMapping("/contacts/{page}")
	public String contacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "contacts - SCM");
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);

		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepo.findContactByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentpage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/user_contacts";
	}

	// fetch user details

	@GetMapping("/user-profile")
	public String profile(Model m) {
		m.addAttribute("title", "Profile - SCM");

		return "normal/user_profile";
	}

	// showing contact details

	@RequestMapping("/contact/{cid}")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model m, Principal principal) {
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		Optional<Contact> optional = this.contactRepo.findById(cid);
		Contact contact = optional.get();
		if (user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
		}
		m.addAttribute("title", contact.getNickname() + " - Details");

		return "normal/contact_details";
	}

	// delete contact

	@RequestMapping("/delete-contact/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Principal principal, HttpSession httpSession) {
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		Optional<Contact> optional = this.contactRepo.findById(cid);
		Contact contact = optional.get();

		if (user.getId() == contact.getUser().getId()) {
			this.contactRepo.delete(contact);
			httpSession.setAttribute("message", new Message("Contact Deleted Successfully", "alert-success"));
		}
		return "redirect:/user/contacts/0";
	}

	// update contact details

	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "update-contact SCM");
		Contact contact = this.contactRepo.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_contact";
	}

	// process contact update
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String processUpdateHandler(@ModelAttribute Contact contact, @RequestParam("pimage") MultipartFile file,
			Model m, HttpSession httpSession, Principal principal) {

		Contact oldcontact = this.contactRepo.findById(contact.getCid()).get();

		try {

			if (!file.isEmpty()) {

				// delete old file image
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldcontact.getImage());
				file1.delete();
				
				

				// set new file image
				String imgName = contact.getCid()+file.getOriginalFilename();
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + imgName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(imgName);
				

			} else {
				contact.setImage(oldcontact.getImage());

			}
			User user = this.userRepo.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepo.save(contact);
			httpSession.setAttribute("message", new  Message("Contact update successfully","alert-success"));
			

		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("contact", contact);
			httpSession.setAttribute("message", new  Message("something went wrong","alert-danger"));
			return "normal/update_contact";
		}
		System.out.println("id" + contact.getCid());

		return "redirect:/user/contact/"+contact.getCid();
	}

	// setting Handler
	@RequestMapping("/user-settings")
	public String setting(Model m) {
		m.addAttribute("title","Setting - SCM");
		
		return "normal/setting";
		
		
	}
	// opening change password handler
	@PostMapping("/change-password")
	public String changePassword(Model m) {
		
		m.addAttribute("title", "Change-Password SCM");
		
		
		return "normal/changepassword";
	}
	
	// process password change
	
	@PostMapping("/process-changePass")
	public String processPasswordChange(@RequestParam("oldpassword")String oldpassword,
			@RequestParam("newpassword")String newpassword,Principal principal,HttpSession session) {
		 System.out.println(oldpassword);
		 System.out.println(newpassword);
		 
		 String username = principal.getName();
		 User currentUser = this.userRepo.getUserByUserName(username);
		 if(this.bCryptPasswordEncoder.matches(oldpassword, currentUser.getPassword())) {
			 
			 // change password
			 currentUser.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
			 currentUser.setAgreement(true);
			 this.userRepo.save(currentUser);
			 session.invalidate();
			 return "redirect:/signin?change=Password change Successfully...";
			 
			 
		 }else {
			 // showing error in current page
			 session.setAttribute("message", new Message("Wrong Old Password","alert-danger"));
			 return "normal/changepassword";
			 
		 }
	}
	
}
