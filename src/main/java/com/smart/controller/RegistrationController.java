package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class RegistrationController {
  
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	//handler for using registering user
	@RequestMapping(value="/do_register",method=RequestMethod.POST)  //post mapping
	public String registerUser(@Valid @ModelAttribute ("user")User user,BindingResult result1,@RequestParam(value="agreement",defaultValue="false") boolean agreement,Model m,RedirectAttributes redirectAttributes)
	{
		try {
		if(!agreement) {
			System.out.println("You are not agreed to terms and conditions");
			throw new Exception("You are not agreed to terms and conditions");
		}
		
		if(result1.hasErrors()) {
			System.out.println("ERROR " +result1.toString());
			//send all the filled data 
			m.addAttribute("user",user);
			return "signup";
		}
		
		
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		System.out.println("Agreemnet "+agreement);
		System.err.println("USER "+user);
		
		User result = this.userRepository.save(user);
		
		m.addAttribute("user",new User());
		//m.addAttribute("user",result);
		
		//session.setAttribute("message", new Message("Successfully Registered!!","alert-success"));
		redirectAttributes.addFlashAttribute("message", new Message("Successfully Registered!!","alert-success"));
		return "redirect:signup";
		
		
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user",user);
			//session.setAttribute("message",new Message("Something went wrong!! "+e.getMessage(), "alert-danger"));
			redirectAttributes.addFlashAttribute("message",new Message("Something went wrong!! "+e.getMessage(), "alert-danger"));
			//return "signup";
			return "redirect:signup";
			}
	}
}
