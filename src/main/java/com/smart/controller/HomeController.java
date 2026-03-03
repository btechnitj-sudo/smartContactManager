package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m) {
		m.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title","Register-Smart Contact Manager");
		  m.addAttribute("user",new User());
		/*
		 * // Always provide a User object for the form on initial load if
		 * (!m.containsAttribute("user")) // Check if user object is already present
		 * (e.g., from error path) { m.addAttribute("user",new User());}
		 * 
		 * // Check if there's a message in the session (e.g., from a redirect after
		 * POST) Object messageObj = session.getAttribute("message"); if (messageObj !=
		 * null) { m.addAttribute("message", messageObj); // Add it to the model for
		 * Thymeleaf to access session.removeAttribute("message"); // ⭐⭐⭐ THIS IS WHERE
		 * YOU USE IT ⭐⭐⭐ System.out.println("Removed message from session."); // For
		 * debugging }
		 */
		 
		return "signup";
	}
	
	//Handler for custom login
	
	@RequestMapping("/signin")
	public String customLogin(Model m) {
		
		m.addAttribute("title","Login Page");
	return "login";
}
}
