package com.swivel.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// tag controller

@RestController
@RequestMapping("/api/tags")
public class TagController {
  private final JdbcTemplate jdbc;
  public TagController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  // returns id + name, list tags for pickers
  @GetMapping
  public List<Map<String,Object>> list() {
    return jdbc.queryForList("""
      SELECT tag_id AS tagId, name, type
      FROM tags
      ORDER BY name
    """);
  }
}
