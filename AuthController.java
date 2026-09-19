package com.swivel.controller;

import com.swivel.model.User;
import com.swivel.recipes.UserService;
import com.swivel.recipes.dto.LoginDto;
import com.swivel.recipes.dto.MeDto;
import com.swivel.recipes.dto.RegisterDto;
import com.swivel.security.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

import java.time.Duration;

// This controllers authority/clearance around different levels.
// it basically allows us to authenticate and give a clearance for an account.

@RestController
@RequestMapping("/auth")
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  private final JwtService jwt;
  private final UserService users;

  public AuthController(JwtService jwt, UserService users) {
    this.jwt = jwt;
    this.users = users;
  }

  // creates a user, hashes their password, and sets JWT cookie
  @PostMapping("/register")
  public ResponseEntity<MeDto> register(@RequestBody RegisterDto dto) {
    try {
      int clr = users.clearanceFromPasscode(dto.getPasscode());
      User u = users.create(dto.getUsername(), dto.getPassword(), dto.getSecPin(), dto.getBio(), clr);
      return ResponseEntity.status(201).body(new MeDto(u.getUserId(), u.getUsername(), u.getClearance(), u.getBio()));
    } catch (IllegalArgumentException iae) {
      if ("USERNAME_TAKEN".equals(iae.getMessage())) return ResponseEntity.status(409).build();
      return ResponseEntity.badRequest().build();
    }
  }

  // verifies their info, issues the JWT HttpOnly Cookie to set the access token
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginDto dto) {
    try {
      User user = users.verify(dto.getUsername(), dto.getPassword());
      String access = jwt.issueAccessToken(user.getUserId(), user.getUsername(), user.getClearance(), 15 * 60);
      ResponseCookie accessCookie = jwt.httpOnlyCookie("ACCESS_TOKEN", access, Duration.ofMinutes(15))
                                       .mutate().sameSite("Lax").secure(false).path("/").build();
      return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .body(new MeDto(user.getUserId(), user.getUsername(), user.getClearance()));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(403).body("INVALID_CREDENTIALS");
    }
  }

  // logs out of course. Clears the authentication cookie.
  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
	  ResponseCookie clearAccess = jwt.httpOnlyCookie("ACCESS_TOKEN", "", Duration.ZERO)
			    .mutate()
			    .maxAge(0)
			    .sameSite("Lax")
			    .secure(false)
			    .path("/")
			    .build();
	  
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccess.toString())
        .build();
  }
}
