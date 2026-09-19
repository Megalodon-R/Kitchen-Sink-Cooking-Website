package com.swivel.recipes.dto;

//dto: data transfer object.
//I believe this just makes for easy transfer for login info.

public class LoginDto {
  private String username;
  private String password;

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
}
