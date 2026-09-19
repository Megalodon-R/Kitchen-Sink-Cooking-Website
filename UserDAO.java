package com.swivel.dao;

import com.swivel.model.User;
import com.swivel.recipes.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

// controls actions you might take with a user. 
// finds users, creates them, controls activation status. 

@Repository
public class UserDAO {

  private final JdbcTemplate jdbc;

  public UserDAO(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // fetches the user with roles and clearance by USERNAME
  // uses AuthController and MeController. Empty or 404 when not found.
  public Optional<User> findByUsername(String username) {
    var sql = """
      SELECT user_id, username, password_hash, sec_pin, bio, clearance, created_at
      FROM users
      WHERE username = ?
    """;
    var list = jdbc.query(sql, (rs, i) -> map(rs), username);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  //fetches the user with roles and clearance by user ID
  // uses AuthController and MeController. Empty or 404 when not found.
  public Optional<User> findById(long userId) {
    var sql = """
      SELECT user_id, username, password_hash, sec_pin, bio, clearance, created_at
      FROM users
      WHERE user_id = ?
    """;
    var list = jdbc.query(sql, (rs, i) -> map(rs), userId);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  // Create user with hashed password
  public User create(String username, String passwordHash, Integer secPin, String bio, int clearance) {
    var sql = "INSERT INTO users(username, password_hash, sec_pin, bio, clearance) VALUES(?,?,?,?,?)";
    KeyHolder kh = new org.springframework.jdbc.support.GeneratedKeyHolder();
    try {
      jdbc.update(con -> {
        var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        ps.setString(2, passwordHash);
        if (secPin == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, secPin);
        if (bio == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, bio);
        ps.setInt(5, clearance);
        return ps;
      }, kh);
    } catch (DataIntegrityViolationException dup) {
      // hits when username isn't unique
      throw new IllegalArgumentException("USERNAME_TAKEN", dup);
    }
    long id = Objects.requireNonNull(kh.getKey()).longValue();
    // fresh db row to include created_at
    return findById(id).orElse(new User(id, username, passwordHash, secPin, bio, clearance, null));
  }
  
  // connects back to AdminUserController
  // updates the username when deactivate takes place. 
  public int deactivateByUsername(String username) {
	  String sql = "UPDATE users SET active = FALSE WHERE username = ?";
	  return jdbc.update(sql, username);
	}
  //connects back to AdminUserController
  // updates the username when RE-activate takes place.
  public int reactivateByUsername(String username) {
	  String sql = "UPDATE users SET active = TRUE WHERE username = ?";
	  return jdbc.update(sql, username);
	}

  //connects back to AdminUserController
  // determines the activeness (if exist) of a username.
  public Boolean isActive(String username) {
	  String sql = "SELECT active FROM users WHERE username = ?";
	  return jdbc.query(sql, ps -> ps.setString(1, username),
	      rs -> rs.next() ? rs.getBoolean(1) : null);
	}
  
  // connects back to AdminUserContoller
  // goes in and releases the username so its reusable in sql sys
  public int releaseUsername(String username) {
	  String sql =
	      "UPDATE users " +
	      "SET username = CONCAT(username, '~disabled-', user_id) " +
	      "WHERE username = ? AND active = FALSE";
	  return jdbc.update(sql, username);
	}
  
  // helper:
  private static User map(ResultSet rs) throws SQLException {
    Timestamp ts = rs.getTimestamp("created_at");
    LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;
    return new User(
      rs.getLong("user_id"),
      rs.getString("username"),
      rs.getString("password_hash"),
      rs.getInt("sec_pin"),
      rs.getString("bio"),
      rs.getInt("clearance"),
      createdAt
    );
  }
}
