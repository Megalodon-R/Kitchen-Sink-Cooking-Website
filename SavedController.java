package com.swivel.controller;

import com.swivel.recipes.dto.RecipeSummaryDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controls saving and unsaving of recipes
// individualizes that list based upon user id

@RestController
@RequestMapping("/api")
public class SavedController {
  private final JdbcTemplate jdbc;
  public SavedController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  
  // saves a recipe for a user
  @PostMapping("/recipes/{id}/save")
  public void save(Authentication auth, @PathVariable int id) {
    var u = auth.getName();
    Long userId = jdbc.queryForObject("SELECT user_id FROM users WHERE username=?", Long.class, u);
    jdbc.update("""
      INSERT INTO saved_recipes(user_id, recipe_id)
      VALUES (?, ?)
      ON DUPLICATE KEY UPDATE saved_at = CURRENT_TIMESTAMP
    """, userId, id);
  }
  
  // unsaves a recipe a user 
  @DeleteMapping("/recipes/{id}/save")
  public void unsave(Authentication auth, @PathVariable int id) {
    var u = auth.getName();
    Long userId = jdbc.queryForObject("SELECT user_id FROM users WHERE username=?", Long.class, u);
    jdbc.update("DELETE FROM saved_recipes WHERE user_id=? AND recipe_id=?", userId, id);
  }

  // list saved recipes for logged-in user
  // uses dtos
  @GetMapping("/me/saved")
  public List<RecipeSummaryDto> meSaved(Authentication auth) {
    var u = auth.getName();
    Long userId = jdbc.queryForObject("SELECT user_id FROM users WHERE username=?", Long.class, u);
    String sql = """
      SELECT r.recipe_id    AS recipeId,
             r.title        AS title,
             r.make_time    AS makeTime,
             u.username     AS createdByName
      FROM saved_recipes sr
      JOIN recipes r ON r.recipe_id = sr.recipe_id
      LEFT JOIN users u ON u.user_id = r.created_by
      WHERE sr.user_id = ?
      ORDER BY sr.saved_at DESC, r.recipe_id DESC
    """;
    return jdbc.query(sql, (rs, i) ->
        new RecipeSummaryDto(
            rs.getInt("recipeId"),
            rs.getString("title"),
            (Integer) rs.getObject("makeTime"),
            rs.getString("createdByName")
        ), userId);
  }
  
  //@PutMapping("/recipes/{id}/rate")
  //public void rate(Authentication auth, @PathVariable int id, @RequestBody RateDto body) {
  //  Long userId = jdbc.queryForObject(
  //      "SELECT user_id FROM users WHERE username=?", Long.class, auth.getName());
  //  jdbc.update("""
  //    INSERT INTO recipe_ratings(recipe_id,user_id,rating)
  //    VALUES (?,?,?)
  //    ON DUPLICATE KEY UPDATE rating=VALUES(rating), rated_at=CURRENT_TIMESTAMP
  // """, id, userId, body.rating());
  //}
}
