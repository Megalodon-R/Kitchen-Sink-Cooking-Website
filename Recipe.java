package com.swivel.recipes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// recipes reborn, I think most of this is redundant as we have the other
// recipe, but after its creation it got mixed in enough that I fear removing it may cause collapse
// you see this applies to a premature version of tags and I believe was moved over as a copy was made
// to overcome package issues. Just two separate workers collision I guess.

public class Recipe {
    private Long recipeId;
    private String title;
    private Integer makeTime;     // minutes
    private String amountage;     // serving size/amount info
    private String steps;         // the steps
    private List<String> tags = new ArrayList<>();
    private Long createdBy;       // user_id
    private String createdByName; // who created it
    private OffsetDateTime createdAt;

    public Recipe() {}

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getMakeTime() { return makeTime; }
    public void setMakeTime(Integer makeTime) { this.makeTime = makeTime; }
    public String getAmountage() { return amountage; }
    public void setAmountage(String amountage) { this.amountage = amountage; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    // helpers for csv tag storage
    public static String toCsv(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(",", tags);
    }
    public static List<String> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(csv.split("\s*,\s*")));
    }
}