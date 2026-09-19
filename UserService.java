package com.swivel.recipes;

import com.swivel.dao.UserDAO;

import com.swivel.model.User;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

// actions connected to user. This deals with passwords, permissions, and verifications.

@Service
public class UserService {
	private final PasswordEncoder pe;
	private final UserDAO dao;
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	// THESE ARE THE DEFAULT PASSCODES.
	// THEY GET OVERRIDDEN IN application.properties.
	// otherwise these are DEFAULT admin and moderator passcodes 
	// respectively for both @value of admin and mod
	@Value("${app.roles.admin.passcode:#{environment.ADMIN_PASSCODE:adminlife}}")
	  private String adminPasscode;
	
	@Value("${app.roles.moderator.passcode:#{environment.MODERATOR_PASSCODE:modlife}}")
	  private String moderatorPasscode;
  
  public UserService(PasswordEncoder pe, UserDAO dao) {
	    this.pe = pe;
	    this.dao = dao;
	  }

  	// creates the account w/ hashed password.
	  public User create(String username, String rawPassword, Integer secPin, String bio, int clearance) {
	    String hash = pe.encode(rawPassword);
	    return dao.create(username, hash, secPin, bio, clearance);
	  }
	  
	  // finds a user by username.
	  public Optional<User> findByUsername(String username) {
	    return dao.findByUsername(username);
	  }

	  // verifies that a username and the password are accurate
	  // in dao
	  // chucks it if it doesn't match
	  public User verify(String username, String rawPassword) {
	    var u = dao.findByUsername(username)
	               .orElseThrow(() -> new RuntimeException("INVALID_CREDENTIALS"));
	    if (!pe.matches(rawPassword, u.getPasswordHash())) {
	      throw new RuntimeException("INVALID_CREDENTIALS");
	    }
	    return u;
	  }
	  
	  // AdminUserController for more info
	  public void deactivateUser(String username) {
		  int n = dao.deactivateByUsername(username);
		  if (n == 0) {
			  throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username);
		  }
	  }
	  
	  // AdminUserController for more info
		public void reactivateUser(String username) {
		  int n = dao.reactivateByUsername(username);
		  if (n == 0) {
			  throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username);
		  }
		}
		
		 public Boolean isActive(String username) {
			    return dao.isActive(username); // returns null is user doesn't exist.
			  }
		 
		// AdminUserController for more info
		public void releaseUsername(String username) {
			  int n = dao.releaseUsername(username);
			  if (n == 0) {
			    throw new ResponseStatusException(HttpStatus.CONFLICT,
			      "Username not released (user not found or still active)");
			  }
			}
	  
	//	Dev only. 
	//	helps verify property loading
	  @PostConstruct
	  void logPasscodes() {
		  log.info("UserService: adminPasscode len={}, moderatorPasscode len={}",
		      adminPasscode == null ? -1 : adminPasscode.length(),
		      moderatorPasscode == null ? -1 : moderatorPasscode.length());
		}
	  
	  // Maps the role passcode to the correct clearance level
	  public int clearanceFromPasscode(String passcode) {
		  int out;
		  if (passcode == null || passcode.isBlank()) out = 0;
		  else if (passcode.equals(adminPasscode)) out = 2;
		  else if (passcode.equals(moderatorPasscode)) out = 1;
		  else out = 0;

		  log.info("clearanceFromPasscode: received='{}' -> {}", passcode, out); // TEMP
		  return out;
		}
}
