package com.swivel.recipes;

import com.swivel.dao.RecipeDAO;
import com.swivel.dao.TagDAO;
import com.swivel.model.Recipe;
import com.swivel.recipes.dto.CreateRecipeDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// creates recipes both with and without pre-established tags

@Service
public class RecipeService {

  private final RecipeDAO recipes;
  private final TagDAO tags;

  public RecipeService(RecipeDAO recipes, TagDAO tags) {
    this.recipes = recipes; this.tags = tags;
  }

  // creating a recipe with no tags.
  @Transactional
  public Recipe create(Recipe payload, long createdByUserId) {
    if (payload == null) throw new IllegalArgumentException("INVALID_BODY");
    if (payload.getTitle() == null || payload.getTitle().isBlank()) {
      throw new IllegalArgumentException("TITLE_REQUIRED");
    }
    payload.setCreatedBy(createdByUserId);
    return recipes.insertRecipe(payload, createdByUserId);
  }

  // accepts tag names, normalizes, and links them
  @Transactional
  public Recipe create(CreateRecipeDto dto, long userId) {
    if (dto == null || dto.getTitle() == null || dto.getTitle().isBlank())
      throw new IllegalArgumentException("TITLE_REQUIRED");

    List<String> names = dto.getTagNames();
    if (names == null) names = java.util.List.of();

    // Resolve/create tag ids
    List<Long> tagIds = tags.findOrCreateManyByNames(names);

    Recipe r = new Recipe();
    r.setTitle(dto.getTitle());
    r.setMakeTime(dto.getMakeTime());
    r.setAmountage(dto.getAmountage());
    r.setSteps(dto.getSteps());
    r.setCreatedBy(userId);

    Recipe created = recipes.insertRecipe(r, userId);      // returns id (int)
    if (!tagIds.isEmpty()) tags.linkRecipeTags(created.getRecipeId(), tagIds);
    return created;
  }

  public void delete(long recipeId) {
	    recipes.deleteById(recipeId);
	  }
  
}
