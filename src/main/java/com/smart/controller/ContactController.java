package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class ContactController {

 

 
 
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	 @Autowired
	 PasswordEncoder passwordEncoder;

  //creating method to avoid writing same code again and again
	//it will run automatically
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName=principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		
		//to check user data coming or not
		//System.out.println("UServ"+user);
		
		m.addAttribute("user",user);
		
	}
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		 
		m.addAttribute("titel","Add Contact");
		m.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	 
	
	
	@PostMapping("/process-contact")
	public String processContact (@ModelAttribute Contact contact,@RequestParam("profile-image")MultipartFile file, Principal principal,RedirectAttributes redirectAttributes ) {
		try {
		String name=principal.getName();
		 User user = userRepository.getUserByUserName(name);
		 
		 //processing and uploading file
		 
		 if(file.isEmpty())
		 {
			 //if the file is empty then try our message
			 System.out.println("file is empty");
			 contact.setImage("contact-default.png");
			 
		 }
		 
		 else {
			 //upload the file to folder and update the name to contact
			 contact.setImage(file.getOriginalFilename());
			 
			 File saveFile = new ClassPathResource("static/image").getFile();
			 Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			 
			 Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
			 
			 System.out.println("Image is uploaded");
			 
		 }
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		userRepository.save(user);
		
		System.out.println("DATA " +contact);
		
		System.out.println("Added to database");
		
		//message success

        // ✅ SET FLASH ATTRIBUTE (success)
        redirectAttributes.addFlashAttribute("message", new com.smart.helper.Message("Your contact is added!! Add more..","success"));
		
		
		}catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			
			//message error
			
			 
	        // ✅ SET FLASH ATTRIBUTE (error)
	        redirectAttributes.addFlashAttribute("message", new com.smart.helper.Message("Some thing went wrong!! Try again", "danger"));
		}
		
		//relative path
		 return "redirect:add-contact";
		 //return "redirect:/user/add-contact"; full path
	}
	
	//show contact handler
	//per page=5[n]
	///current page=0[page]
	
	@GetMapping({"/show_contacts/{page}"})
	public String showContacts(@PathVariable("page")Integer page ,Model m,Principal principal) {
		
		m.addAttribute("title","View Contacts");
		
		//user ki list bhejni hai
		String userName = principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		
		//pageable object have currentPage and contact per page
		
		Pageable pageable = PageRequest.of(page,5);
		
		 Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		 
		 m.addAttribute("contacts",contacts);
		 
		 m.addAttribute("currentPage",page);
		 
		 m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
		
	}
	
	//showing particular contact details
	
	@GetMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model m,Principal principal) {
		
		System.out.println("CId"+cid);
		//getting user logged in 
	String userName = principal.getName();
				
	User user = userRepository.getUserByUserName(userName);
		
		Optional<Contact> contactOptional = contactRepository.findById(cid);
		
		if (contactOptional.isPresent()) {
		Contact contact = contactOptional.get();
		
		
		
		if(user.getId()==contact.getUser().getId()) {
		
		m.addAttribute("contact",contact);
		m.addAttribute("title",contact.getName());
		
		return "normal/contact_detail";
		}
		}
		return "normal/access_denied";
	}
	
	//delete contact handler
	
	@PostMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Principal principal,RedirectAttributes redirectAttributes) {
		
		Optional<Contact> contactOptional = contactRepository.findById(cid);
		
		  if (contactOptional.isEmpty()) {
		        redirectAttributes.addFlashAttribute("message", new com.smart.helper.Message ("Contact not found.", "danger"));
		        return "redirect:/user/show-contacts/0";
		    }
		
		Contact contact = contactOptional.get();
		
		//get the user logged in
		 
		String userName = principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		 
		if(user.getId()==contact.getUser().getId()) {
			
			  //remove image
			 try {
		            // --- STEP 1: DELETE THE ASSOCIATED PICTURE ---

		            // Get the image name BEFORE deleting the contact from the DB
		            final String imageName = contact.getImage();

		            // Crucial Safety Check: Do not delete the default image
		            if (imageName != null && !imageName.equals("contact-default.png")) {
		                
		                // Get the full path to the image
		                File saveFile = new ClassPathResource("static/image").getFile();
		                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);
		                
		                // Delete the file
		                Files.deleteIfExists(path);
		                
		                System.out.println("Deleted image: " + imageName);
		            }
			
			 // This is the new, cleaner logic:
	        user.getContacts().remove(contact); // Remove the child from the parent's list
	        
	        
	      
	        
	        
	        // By saving the parent, JPA will see the child was removed and,
	        // due to orphanRemoval=true, it will automatically DELETE the contact from the DB.
	        this.userRepository.save(user); 
			
	        //now we are not using orphanRemoval=true then below method apply
	        //contact.setUser(null);
			
		//contactRepository.delete(contact);
		
		redirectAttributes.addFlashAttribute("message",new com.smart.helper.Message("Contact deleted successfully", "success"));
		} catch (Exception e) {
            // Log the error and notify the user
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", new com.smart.helper.Message("Error deleting contact. " + e.getMessage(), "danger"));
        }
    } else {
        // If the user tries to delete a contact they don't own
        redirectAttributes.addFlashAttribute("message", new com.smart.helper.Message("You are not authorized to perform this action.", "danger"));
    }
		return "redirect:/user/show_contacts/0";
		
	}
	
	
	//open update form handler
	
	@GetMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Principal principal,Model m,RedirectAttributes redirectAttributes) {
		 m.addAttribute("title","Update Contact");
		 
		 Optional<Contact> contactOptional = contactRepository.findById(cid);
		 if (contactOptional.isEmpty()) {
		        redirectAttributes.addFlashAttribute("message",
		                new com.smart.helper.Message("Contact not found!", "danger"));
		        return "redirect:/user/show_contacts/0";
		    }

		 Contact contact = contactOptional.get();
		 
		 // 🔒 MANDATORY SECURITY CHECK
		    // Prevent users from editing other users' contacts by changing URL
		    User loggedInUser = userRepository.getUserByUserName(principal.getName());
		    if (contact.getUser().getId()!=(loggedInUser.getId())) {
		        
		        redirectAttributes.addFlashAttribute("message",
		                new com.smart.helper.Message("You are not allowed to edit this contact!", "danger"));
		        return "redirect:/user/show_contacts/0";
		    }
		
		m.addAttribute("contact",contact);
	
	return "normal/update_form";
	}
	
	//update contact handler
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profile-image")  MultipartFile file,Model m,RedirectAttributes redirectAttributes,Principal principal) {
		
		
		try {
			
			 // 1) load logged-in user
	        User loggedInUser = userRepository.getUserByUserName(principal.getName());
			
			//old contact details
			 Contact oldContactDetail = contactRepository.findById(contact.getCid()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
			
			// 3) ownership check (IMPORTANT)
		        if (oldContactDetail.getUser() == null || oldContactDetail.getUser().getId() !=loggedInUser.getId()) {
		            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
		        }
		        
		     // 4) update only allowed fields (avoid trusting whole entity from client)
		        oldContactDetail.setName(contact.getName());
		        oldContactDetail.setEmail(contact.getEmail());
		        oldContactDetail.setPhone(contact.getPhone());
		        oldContactDetail.setDescription(contact.getDescription());

		        
		        // 5) handle image
		        
		        
		     // basic validation
	            String contentType = file.getContentType();
	            if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png")||contentType.equals("image/jpg"))) {
	                redirectAttributes.addFlashAttribute("message",
	                        new com.smart.helper.Message("Only JPG,JPEG/PNG images allowed", "danger"));
	                return "redirect:/user/update-contact/" + oldContactDetail.getCid();
	            }
			 
	            // If a new file is provided by the user
	            if (!file.isEmpty()) {
	                File imageDir = new ClassPathResource("static/image").getFile();

	                // Delete old photo if it exists and is not the default
	                String oldImageName = oldContactDetail.getImage();
	                if (oldImageName != null && !oldImageName.isBlank() && !oldImageName.equals("contact-default.png"))  {
	                    File oldFile = new File(imageDir, oldImageName);
	                    if (oldFile.exists()) {
	                        // Attempt to delete. No explicit warning if it fails without a logger.
	                        oldFile.delete();
	                    }
	                }

	                // Save new photo
	                // Generate a unique filename to prevent collisions and enhance security
	                String newFileName = java.util.UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
	                Path path = Paths.get(imageDir.getAbsolutePath(), newFileName);

	                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	                oldContactDetail.setImage(newFileName); // Set new image name on the EXISTING contact
	            }
	            // If 'file' is empty (no new image uploaded), existingContact.getImage()
	            // will retain its current value, so no 'else' block is needed here.

	            // 6) Save the EXISTING contact (which has all the updates, including the new image name if any)
	            contactRepository.save(oldContactDetail);

	            redirectAttributes.addFlashAttribute("message",
	                    new com.smart.helper.Message("Your contact is updated", "success"));

	            // Redirect using the cid of the EXISTING contact
	            return "redirect:/user/" + oldContactDetail.getCid() + "/contact";

	        } catch (Exception e) {
	            e.printStackTrace(); // Printing stack trace for debugging
	            redirectAttributes.addFlashAttribute("message",
	                    new com.smart.helper.Message("Update failed: " + e.getMessage(), "danger"));
	            // Redirect back to the update form for the same contact (using cid from the form)
	}
	
		 return "redirect:/user/update-contact/" + contact.getCid();
    }
	

    // THIS IS THE HELPER METHOD THAT NEEDS TO BE INSIDE THIS CLASS
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

	//your profile handler
	@GetMapping("/profile")
	
	public String yourProfile(Model m) {
		 
		m.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
	
	@GetMapping("/settings")
	public String showSettings(Model model) {
	    model.addAttribute("title", "Settings");
	    // No need to add user again! @ModelAttribute already did it
	    return "normal/settings";
	}
    
 // Add these methods to your existing ContactController class

    @PostMapping("/update-profile")
    public String updateProfile(
        @RequestParam("name") String name,
        @RequestParam("about") String about,
        Principal principal,
        RedirectAttributes redirectAttributes) {

        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);

        // Update profile information
        user.setName(name);
        user.setAbout(about);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message",
            new com.smart.helper.Message("Profile updated successfully!", "success"));

        return "redirect:/user/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(
        @RequestParam("oldPassword") String oldPassword,
        @RequestParam("newPassword") String newPassword,
        @RequestParam("confirmPassword") String confirmPassword,
        Principal principal,
        RedirectAttributes redirectAttributes
       ) {

        // Validate that new password and confirmation match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("message",
                new com.smart.helper.Message("New password and confirmation do not match!", "danger"));
            return "redirect:/user/settings";
        }

        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);

        // Check if old password is correct
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("message",
                new com.smart.helper.Message("Current password is incorrect!", "danger"));
            return "redirect:/user/settings";
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message",
            new com.smart.helper.Message("Password changed successfully!", "success"));

        return "redirect:/user/settings";
    }
	}
