package com.swivel.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// this is responsible for ratings in recipes.

record RateDto(int rating) {}

@RestController
@RequestMapping("/api")
public class RatingController {
  private final JdbcTemplate jdbc;
  public RatingController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  // sets the rating for a recipe made by user.
  @PutMapping("/recipes/{id}/rate")
  public void rate(Authentication auth, @PathVariable int id, @RequestBody RateDto body) {
    if (body.rating() < 1 || body.rating() > 5) throw new IllegalArgumentException("rating 1..5");
    var user = jdbc.queryForObject("SELECT user_id FROM users WHERE username=?", Long.class, auth.getName());
    jdbc.update("""
      INSERT INTO recipe_ratings(recipe_id, user_id, rating)
      VALUES (?, ?, ?)
      ON DUPLICATE KEY UPDATE rating=VALUES(rating), rated_at=CURRENT_TIMESTAMP
    """, id, user, body.rating());
  }
}
