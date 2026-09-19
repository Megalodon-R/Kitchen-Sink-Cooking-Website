package com.swivel.controller;
import org.springframework.security.core.Authentication;

import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// controls comments, listing out comments
// authenticating the user it belongs to, collecting relevant info.

@RestController
@RequestMapping("/api/recipes/{recipeId}/comments")
public class RecipeCommentsController {
  private final JdbcTemplate jdbc;
  public RecipeCommentsController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  record CommentDto(String message, Integer rating) {}

  // adds a comment, creates an ID for it.
  // look to dtos for more info.
  @PostMapping
  public void add(Authentication auth, @PathVariable int recipeId, @RequestBody CommentDto dto) {
    Long userId = jdbc.queryForObject("SELECT user_id FROM users WHERE username=?", Long.class, auth.getName());
    jdbc.update("INSERT INTO comments(recipe_id,user_id,rating,message) VALUES (?,?,?,?)",
      recipeId, userId, dto.rating(), dto.message());
  }

  	// lists the existing comments.
  @GetMapping
  public List<Map<String,Object>> list(@PathVariable int recipeId) {
    return jdbc.queryForList("""
      SELECT c.comment_id,
             c.user_id,
             u.username,
             c.rating,
             c.message,
             c.created_at
      FROM comments c
      JOIN users u ON u.user_id = c.user_id
      WHERE c.recipe_id = ?
      ORDER BY c.created_at DESC
    """, recipeId);
  }
}
