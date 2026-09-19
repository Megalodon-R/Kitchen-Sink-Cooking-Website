package com.swivel.dao;

import com.swivel.model.Recipe;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

// DAO or Direct Access Object is part of the service layer
// this handles all back and forths involving recipes, their attachments, creators, holders etc.

@Repository
public class RecipeDAO {

  private final DataSource dataSource;
  private final NamedParameterJdbcTemplate jdbc;
  public RecipeDAO(DataSource dataSource) {
	    this.dataSource = dataSource;
	    this.jdbc = new NamedParameterJdbcTemplate(dataSource);
	  }
		  
  private static final String INSERT_SQL = """
    INSERT INTO recipes (title, make_time, amountage, steps, created_by)
    VALUES (?, ?, ?, ?, ?)
  """;

  // inserts into recipes and returns the generated key.
  public Recipe insertRecipe(Recipe r, long createdByUserId) {
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(1, r.getTitle());
      if (r.getMakeTime() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, r.getMakeTime());
      ps.setString(3, r.getAmountage());
      ps.setString(4, r.getSteps());
      ps.setLong(5, createdByUserId);

      ps.executeUpdate();

      try (ResultSet keys = ps.getGeneratedKeys()) {
    	  if (!keys.next()) throw new SQLException("No recipe ID returned");
    	  r.setRecipeId(keys.getInt(1));
    	  return r;
    	}

    } catch (SQLException e) {
      throw new RuntimeException("insertRecipe failed: " + e.getMessage(), e);
    }
  }

  // inserts the batch into recipe_tags.
  // ignores the duplicates
  // TAGS CANNOT BE NULL
  // IT WILL AUTO ASSUME THEM CANONIZED
  public void linkRecipeTags(int recipeId, List<Long> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) return;

    final String SQL = "INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES (?, ?)";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(SQL)) {

      for (Long tagId : new LinkedHashSet<>(tagIds)) {
        if (tagId == null) continue;
        ps.setInt(1, recipeId);
        ps.setLong(2, tagId);
        ps.addBatch();
      }
      ps.executeBatch();

    } catch (SQLException e) {
      throw new RuntimeException("linkRecipeTags failed: " + e.getMessage(), e);
    }
  }
  
  // Deletes recipe by id
  public void deleteById(long recipeId) {
	    final String sql = "DELETE FROM recipes WHERE recipe_id = ?";

	    try (Connection conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	      ps.setLong(1, recipeId);
	      ps.executeUpdate();

	    } catch (SQLException e) {
	      throw new RuntimeException("deleteById failed: " + e.getMessage(), e);
	    }
	  }
  
  public List<Map<String, Object>> search(
		    String q,
		    String author,
		    List<Long> includeIds,
		    List<Long> excludeIds,
		    int page,
		    int size
		) {
		  StringBuilder sql = new StringBuilder();
		  sql.append("SELECT ")
		     .append("  r.recipe_id  AS recipeId, ")
		     .append("  r.title      AS title, ")
		     .append("  r.make_time  AS makeTime, ")
		     .append("  r.amountage  AS amountage, ")
		     .append("  r.created_at AS createdAt, ")
		     .append("  u.username   AS createdByName ")
		     .append("FROM recipes r ")
		     .append("LEFT JOIN users u ON u.user_id = r.created_by ")
		     .append("WHERE 1=1 ");

		  Map<String, Object> p = new HashMap<>();

		  // free-text search across title, steps, username
		  if (q != null && !q.isBlank()) {
		    sql.append(" AND (")
		       .append("   LOWER(r.title) LIKE LOWER(CONCAT('%', :q, '%')) ")
		       .append("   OR LOWER(r.steps) LIKE LOWER(CONCAT('%', :q, '%')) ")
		       .append("   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) ")
		       .append(" ) ");
		    p.put("q", q.trim());
		  }

		  // explicit author filter (still matches username)
		  if (author != null && !author.isBlank()) {
		    sql.append(" AND LOWER(u.username) LIKE LOWER(CONCAT('%', :author, '%')) ");
		    p.put("author", author.trim());
		  }

		  // behind the tag filtering excludes
		  // "EXCLUDE: recipe must have NONE of these tags"
		  if (excludeIds != null && !excludeIds.isEmpty()) {
		    sql.append(" AND NOT EXISTS (")
		       .append("   SELECT 1 FROM recipe_tags rtX ")
		       .append("   WHERE rtX.recipe_id = r.recipe_id ")
		       .append("     AND rtX.tag_id IN (:excludeIds)")
		       .append(" ) ");
		    p.put("excludeIds", excludeIds);
		  }

		  // behind the filtering includes
		  // "INCLUDE (ANY): recipe must have AT LEAST ONE of these tags"
		  if (includeIds != null && !includeIds.isEmpty()) {
		    sql.append(" AND EXISTS (")
		       .append("   SELECT 1 FROM recipe_tags rtI ")
		       .append("   WHERE rtI.recipe_id = r.recipe_id ")
		       .append("     AND rtI.tag_id IN (:includeIds)")
		       .append(" ) ");
		    p.put("includeIds", includeIds);
		  }

		  int pageSize = Math.min(Math.max(size, 1), 100);
		  int offset = Math.max(page, 0) * pageSize;

		  sql.append(" ORDER BY r.created_at DESC ")
		     .append(" LIMIT :limit OFFSET :offset ");

		  p.put("limit", pageSize);
		  p.put("offset", offset);

		  return jdbc.queryForList(sql.toString(), p);
		}
}