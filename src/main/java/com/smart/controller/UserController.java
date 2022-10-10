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
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String name = principal.getName();
		
		User user = this.userRepo.getUserByUserName(name);
		
		m.addAttribute("user", user);
	}
	
	// user dashboard
	@RequestMapping("/index")
	public String dashboard(Model m) {
		
		m.addAttribute("title", "Dashboard - Smartcontact");

		return "normal/user_dashboard";
	}
	
	//user add contact
	
	@GetMapping("/add-contact")
	public String addcontact(Model m) {
		
		m.addAttribute("title", "AddContact - smartcontact");
		m.addAttribute("contact",new Contact());
		return "normal/user_addcontact";
	}
	// save contact to database
	@PostMapping("/process-contact")
	public String savecontact(@ModelAttribute Contact contact,@RequestParam("pimage")MultipartFile file,Principal principal,
			Model m,HttpSession httpSession) {
		
		try {
			
			if(file.isEmpty()) {
				
				contact.setImage("contact.png");
				
				
			}else {
				// save the file url into contact -> database
				contact.setImage(file.getOriginalFilename());
				
				// save the file url into folder img
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is upload");
			}
					
			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepo.save(user);
			System.out.println("data"+contact);
			httpSession.setAttribute("message", new Message("contact Added successfully","alert-success"));
			
			return "normal/user_addcontact";
			
		}catch (Exception e) {
			
			System.out.println(e.getMessage());
			m.addAttribute("contact", contact);
			httpSession.setAttribute("messge", new Message("something went wrong","alert-danger"));
			
			return "normal/user_addcontact";
		}		
	}

	//fetch specify user contact 
	@GetMapping("/contacts/{page}")
	public String contacts(@PathVariable("page")Integer page,Model m,Principal principal) {
		m.addAttribute("title", "contacts - smart contact");
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepo.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentpage",page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/user_contacts";
	}
	
	// fetch user details
	
	@GetMapping("/user-profile")
	public String profile(Model m) {
		m.addAttribute("title","Profile - smartcontact");
		
		
		return "normal/user_profile";
	}
	@RequestMapping("/contact/{cid}")
	public String showContactDetails(@PathVariable("cid")Integer cid,Model m,Principal principal) {
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		Optional<Contact> optional = this.contactRepo.findById(cid);
		Contact contact = optional.get();
		if(user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
		}
		
		m.addAttribute("title", contact.getNickname()+" - Details");
		
		return "normal/contact_details";
		
	}
}

