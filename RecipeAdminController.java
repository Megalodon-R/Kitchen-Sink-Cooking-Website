package com.swivel.controller;

import com.swivel.model.Recipe;
import com.swivel.model.User;
import com.swivel.recipes.RecipeService;
import com.swivel.recipes.UserService;
import com.swivel.recipes.dto.CreateRecipeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// lot of authentication based upon if you're an admin
// related to creation and deletion of recipes.

@RestController
@RequestMapping("/api/admin/recipes")   
public class RecipeAdminController {

  private static final Logger log = LoggerFactory.getLogger(RecipeAdminController.class);

  private final RecipeService service;
  private final UserService users;

  public RecipeAdminController(RecipeService service, UserService users) {
    this.service = service;
    this.users  = users;
  }

  // Creates a recipe
  // links tags to the created recipe.
  // POST /api/admin/recipes
  @PostMapping
  public ResponseEntity<?> create(@RequestBody CreateRecipeDto payload,
                                  Authentication auth) {

    log.info("ADMIN CREATE: user={} auths={}",
        auth != null ? auth.getName() : "null",
        auth != null ? auth.getAuthorities() : "null");

    if (auth == null || !auth.isAuthenticated()) {
      return ResponseEntity.status(401).body("UNAUTHENTICATED");
    }

    long userId = users.findByUsername(auth.getName())
        .map(User::getUserId)
        .orElseThrow(() -> new IllegalStateException("User not found"));

    Recipe created = service.create(payload, userId);

    return ResponseEntity
        .created(URI.create("/api/recipes/" + created.getRecipeId()))
        .body(created);
  }
  
  // allows admins to delete a recipe (hard delete).
  // DELETE /api/admin/recipes/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
