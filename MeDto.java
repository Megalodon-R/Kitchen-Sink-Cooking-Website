package com.swivel.recipes.dto;

//dto: data transfer object.
//I believe this just makes for easy transfer for personal account info.

public class MeDto {
  private long userId;
  private String username;
  private int clearance;
  private String bio; // may be null

  public MeDto() {}

  public MeDto(long userId, String username, int clearance) {
    this.userId = userId;
    this.username = username;
    this.clearance = clearance;
    this.bio = null;
  }

  public MeDto(long userId, String username, int clearance, String bio) {
    this.userId = userId;
    this.username = username;
    this.clearance = clearance;
    this.bio = bio;
  }

  public long getUserId() { return userId; }
  public String getUsername() { return username; }
  public int getClearance() { return clearance; }
  public String getBio() { return bio; }
}
