package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserLoginController {

	@Autowired
	private UserRepository userRepository;
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal) {
		
		m.addAttribute("title","User Dashboard");
		
		String userName = principal.getName();
		System.out.println("UserName "+ userName);
		
		//get the user using username(Email)
		
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("user "+user);
		
		m.addAttribute("user",user);
		
		return "normal/user_dashboard";
	}
}
