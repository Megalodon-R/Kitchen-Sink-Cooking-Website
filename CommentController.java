package com.swivel.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// make sure you have this import:
import org.springframework.web.bind.annotation.DeleteMapping;

// this controls comment clearance and destruction of that comment Id when deleting

@RestController
@RequestMapping("/api/comments")
public class CommentController {
  private final JdbcTemplate jdbc;
  public CommentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  
 // Delete comment (Hard delete). Has permissions so only base User can delete their own.
 // Any mod or admin can delete anyone's comment.
 //Delete comment (hard delete) (admin/mod)
  @DeleteMapping("/{id:[0-9]+}")
  public void delete(Authentication auth, @PathVariable long id) {
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      throw new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    final String username = auth.getName();
    final Long callerId = jdbc.queryForObject(
      "SELECT user_id FROM users WHERE username = ?",
      Long.class, username);

    final boolean isOwner = Boolean.TRUE.equals(jdbc.queryForObject(
      "SELECT COUNT(*) > 0 FROM comments WHERE comment_id = ? AND user_id = ?",
      Boolean.class, id, callerId));

    if (!isOwner) {
      final var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
      final boolean isStaff = roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MODERATOR");
      if (!isStaff) {
        throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN, "Not allowed to delete others' comments");
      }
    }

    final int n = jdbc.update("DELETE FROM comments WHERE comment_id = ?", id);
    if (n == 0) {
      throw new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.NOT_FOUND, "Comment not found");
    }
  }
}
