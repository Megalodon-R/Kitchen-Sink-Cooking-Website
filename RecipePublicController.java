package com.swivel.controller;

import com.swivel.dao.RecipeDAO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// lots of tag control for recipes and filtering

@RestController
@RequestMapping("/api/recipes")
public class RecipePublicController {

  private final JdbcTemplate jdbc;
  private final RecipeDAO recipeDao;

  public RecipePublicController(JdbcTemplate jdbc, RecipeDAO recipeDao) {
    this.jdbc = jdbc;
    this.recipeDao = recipeDao;
  }

  // List recipes + include (all) + exclude (none) tag filtering
  @GetMapping
  public List<Map<String, Object>> listWithFilters(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String author,
      @RequestParam(required = false) String include,
      @RequestParam(required = false) String exclude,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    List<Long> includeIds = parseIds(include);
    List<Long> excludeIds = parseIds(exclude);
    return recipeDao.search(q, author, includeIds, excludeIds, page, size);
  }

  // fetches a single recipe by id
  @GetMapping("/{id}")
  public Map<String,Object> detail(@PathVariable int id) {
    return jdbc.queryForMap("""
      SELECT
        r.recipe_id  AS recipeId,
        r.title      AS title,
        r.make_time  AS makeTime,
        r.amountage  AS amountage,
        r.steps      AS steps,
        r.created_at AS createdAt,
        u.username   AS createdByName
      FROM recipes r
      LEFT JOIN users u ON u.user_id = r.created_by
      WHERE r.recipe_id = ?
    """, id);
  }

  // Parses Strings into a list of Longs (ignores blanks)
  private static List<Long> parseIds(String csv) {
    if (csv == null || csv.isBlank()) return List.of();
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> {
          try { return Long.parseLong(s); }
          catch (Exception e) { return null; }
        })
        .filter(Objects::nonNull)
        .distinct()
        // this limit is for safety. in case someone tries to
        // somehow filter over 20 tags. 
        .limit(20)
        .collect(Collectors.toList());
  }
}
