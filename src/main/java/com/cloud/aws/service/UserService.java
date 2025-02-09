package com.cloud.aws.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloud.aws.model.Invoice;
import com.cloud.aws.model.User;
import com.cloud.aws.repository.InvoiceRepository;
import com.cloud.aws.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository ;
    
        
    @Autowired
	private JavaMailSender mailSender;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

	private void sendVendorRegistrationConfirmationEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom("2024mt03108@wilp.bits-pilani.ac.in");
		mailSender.send(message);

	}
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
    	String subject ="Sign Up Request";
    	user.setPassword(passwordEncoder.encode(user.getPassword()));
    	sendVendorRegistrationConfirmationEmail(user.getEmail(), subject, "Hi "+ user.getName()+ "Your "+subject +" Successfully");
        return userRepository.save(user);
    }
    
	public boolean validateUser(String email, String password) {
		List<User> user = userRepository.findByEmail(email);
		if (user != null & !user.isEmpty() && passwordEncoder.matches(password, user.get(0).getPassword())) {			
			return true;
		} else {
			return false; // User not found
		}
	}
	
	public boolean validateUserPwd(String email) {
		List<User> user = userRepository.findByEmail(email);
		if (user != null && !user.isEmpty()) {			
			return true;
		} else {
			return false; // User not found
		}
	}

	public User isEmailVerified(String email) {
		// Check if the email exists and is verified
		List<User> user = null;
		user = userRepository.findByEmail(email);
		if (user != null & !user.isEmpty())
			return user.get(0);
		return user.get(0);
	}
	
	public User isFindById(long id) {
		// Check if the email exists and is verified
		Optional<User> user = null;
		user = userRepository.findById(id);
		if (user != null & !user.isEmpty())
			return user.get();
		else 
			return user.get();
	}


	public List<Invoice> retrieveDataByUserId(Long userId) {
		List<Invoice> inv=  invoiceRepository.findInvoicesByUserId(userId);
		return inv;
	}
	
	public Invoice retrieveDataById(Long Id) {
		Invoice inv=  invoiceRepository.findInvoicesById(Id);
		return inv;
	}

	public User updateUser(User user) {
		String subject ="Password Reset Request";
		List<User> user2 = userRepository.findByEmail(user.getEmail());
		User user1 = user2.get(0);
		user1.setCreateDt(LocalDateTime.now());
		user1.setName(user.getName() != null ? user.getName() : user1.getName());
    	user1.setPassword(passwordEncoder.encode(user.getPassword()));
    	sendVendorRegistrationConfirmationEmail(user.getEmail(), subject, "Hi "+ user.getName()+ "Your "+subject +" Successfully");
        return userRepository.save(user1);
	}
    
}