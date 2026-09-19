package com.swivel.controller;

import com.swivel.recipes.UserService;
import com.swivel.recipes.dto.MeDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// this doesn't do much. It returns a current user.

@RestController
@RequestMapping("/api")
public class MeController {
  private final UserService users;
  public MeController(UserService users) { this.users = users; }

  //returns the current user (so that authentication can work).
  @GetMapping("/me")
  public MeDto me(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    var u = users.findByUsername(auth.getName()).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
    );
    return new MeDto(u.getUserId(), u.getUsername(), u.getClearance(), u.getBio());
  }
}
