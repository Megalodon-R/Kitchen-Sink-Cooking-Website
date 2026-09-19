package com.swivel.recipes.dto;

//dto: data transfer object.
//I believe this just makes for easy transfer for recipe info.

public class RecipeSummaryDto {
  private int recipeId;
  private String title;
  private Integer makeTime;       
  private String createdByName;

  public RecipeSummaryDto(int recipeId, String title, Integer makeTime, String createdByName) {
    this.recipeId = recipeId;
    this.title = title;
    this.makeTime = makeTime;
    this.createdByName = createdByName;
  }

  public int getRecipeId() { return recipeId; }
  public String getTitle() { return title; }
  public Integer getMakeTime() { return makeTime; }
  public String getCreatedByName() { return createdByName; }
}
