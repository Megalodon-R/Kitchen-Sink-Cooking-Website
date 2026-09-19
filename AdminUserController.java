package com.swivel.controller;

import com.swivel.recipes.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// this controls activity/deactivate status of an account.
// it can be used in powershell to free up a username.

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final UserService users;

  public AdminUserController(UserService users) {
    this.users = users;
  }

  // deactivate a user by username
  // admin must be logged in, performed through
  // powershell advised ("201 notes" has notes for command)
  @DeleteMapping("/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deactivate(@PathVariable String username) {
    users.deactivateUser(username);
    return ResponseEntity.noContent().build();
  }

  // helper for reactivating a previously deactivated user
 //admin must be logged in, performed through
 // powershell ("201 notes" has notes for commands)
  @PostMapping("/{username}/reactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> reactivate(@PathVariable String username) {
    users.reactivateUser(username);
    return ResponseEntity.noContent().build();
  }

   // for releasing a deactivated username for reuse (renames the old row)
   // only call after de-activation. 
   // you can create a new account with the same username after.
 //admin must be logged in, performed through
 // powershell ("201 notes" has notes for commands)
  @PostMapping("/{username}/release-username")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> release(@PathVariable String username) {
    users.releaseUsername(username);
    return ResponseEntity.noContent().build();
  }
}
