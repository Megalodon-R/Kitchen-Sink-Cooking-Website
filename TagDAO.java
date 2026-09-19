package com.swivel.dao;

import com.swivel.recipes.TagNormalizer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

// this is for tags: gets rid of duplicates, updates tags values,
// manages them and their names.

@Repository
public class TagDAO {

  private final JdbcTemplate jdbc;
  private final NamedParameterJdbcTemplate np;

  public TagDAO(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
    this.np = new NamedParameterJdbcTemplate(jdbc);
  }
  
  // ~ normalization helpers ~

  // collapse to canonical string used as a key ("eggs" -> "egg")
  private static String canonicalize(String raw) {
    return TagNormalizer.normalize(raw);
  }

  private static List<String> canonicalizeAll(Collection<String> raws) {
    if (raws == null) return List.of();
    return raws.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(TagNormalizer::normalize)
        .distinct()
        .collect(Collectors.toList());
  }

  // ~ create and find ~

  // bulk find: Canonical -> tag_id
  public Map<String, Long> findIdsByCanonicals(Collection<String> canonicals) {
	    if (canonicals == null || canonicals.isEmpty()) return Map.of();

	    String sql = "SELECT tag_id, canonical FROM tags WHERE canonical IN (:cans)";
	    Map<String, Object> params = Map.of("cans", canonicals);

	    Map<String, Long> out = new HashMap<>();
	    // use queryForList instead of np.query(...) with a lambda
	    List<Map<String, Object>> rows = np.queryForList(sql, params);
	    for (Map<String, Object> row : rows) {
	      String canonical = (String) row.get("canonical");
	      Long id = ((Number) row.get("tag_id")).longValue();
	      out.put(canonical, id);
	    }
	    return out;
	  }

  // Create a single tag if missing. Returns id (existing or new).
  // Requires a unique index on tags.canonical.
  public long findOrCreateOne(String displayName) {
    String can = canonicalize(displayName);
    if (can == null || can.isBlank()) throw new IllegalArgumentException("Empty tag");

    // Try existing first
    Long existing = jdbc.query(
        "SELECT tag_id FROM tags WHERE canonical=?",
        (rs, i) -> rs.getLong(1),
        can
    ).stream().findFirst().orElse(null);
    if (existing != null) return existing;

    // Insert (on duplicates it handles; 
    // LAST_INSERT_ID returns existing id if duplicate)
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(con -> {
      String sql = "INSERT INTO tags(name, canonical, type) VALUES(?,?,1) " +
                   "ON DUPLICATE KEY UPDATE tag_id=LAST_INSERT_ID(tag_id)";
      PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, displayName.trim());
      ps.setString(2, can);
      return ps;
    }, kh);
    return Objects.requireNonNull(kh.getKey()).longValue();
  }

  // Bulk find/create. Returns IDs in the order they first appear
  public List<Long> findOrCreateManyByNames(Collection<String> rawNames) {
    if (rawNames == null) return List.of();

    // Keeps first-seen display name based on canonical name
    LinkedHashMap<String, String> canonToDisplay = new LinkedHashMap<>();
    for (String raw : rawNames) {
      if (raw == null) continue;
      String trimmed = raw.trim();
      if (trimmed.isEmpty()) continue;
      String can = canonicalize(trimmed);
      if (can == null || can.isBlank()) continue;
      canonToDisplay.putIfAbsent(can, trimmed);
    }
    if (canonToDisplay.isEmpty()) return List.of();

    // Bulk looking up for existing ids
    Map<String, Long> existing = findIdsByCanonicals(canonToDisplay.keySet());

    // create missing
    List<Long> createdIds = new ArrayList<>();
    for (Map.Entry<String, String> e : canonToDisplay.entrySet()) {
      String can = e.getKey();
      String display = e.getValue();
      Long id = existing.get(can);
      if (id == null) {
        id = findOrCreateOne(display); // uses on duplicate + LAST_INSERT_ID trick
      }
      createdIds.add(id);
    }

    // gets rid of duplicates while preserving order
    return createdIds.stream().distinct().collect(Collectors.toList());
  }

  // ~ Linking ~

  // Link recipe to tag IDs (also ignores duplicates)
  public void linkRecipeTags(int recipeId, List<Long> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) return;
    jdbc.batchUpdate(
        "INSERT IGNORE INTO recipe_tags (recipe_id, tag_id) VALUES (?,?)",
        tagIds,
        tagIds.size(),
        (ps, tagId) -> {
          ps.setInt(1, recipeId);
          ps.setLong(2, tagId);
        }
    );
  }
}
