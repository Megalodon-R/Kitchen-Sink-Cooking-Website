package com.swivel.recipes.dto;

// dto: data transfer object.
// I believe this just makes for easy transfer for recipe info.

import java.util.List;

public class CreateRecipeDto {
  private String title;
  private Integer makeTime;
  private String amountage;
  private String steps;
  private List<String> tagNames;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public Integer getMakeTime() { return makeTime; }
  public void setMakeTime(Integer makeTime) { this.makeTime = makeTime; }
  public String getAmountage() { return amountage; }
  public void setAmountage(String amountage) { this.amountage = amountage; }
  public String getSteps() { return steps; }
  public void setSteps(String steps) { this.steps = steps; }
  public List<String> getTagNames() { return tagNames; }
  public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }
}
