
package com.swivel.recipes.dto;

//dto: data transfer object.
//I believe this just makes for easy transfer for register info.

public class RegisterDto {
  private String username;
  private String password;
  private Integer secPin;
  private String passcode;
  private String bio;

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public Integer getSecPin() { return secPin; }
  public void setSecPin(Integer secPin) { this.secPin = secPin; }

  public String getPasscode() { return passcode; }
  public void setPasscode(String passcode) { this.passcode = passcode; }

  public String getBio() { return bio; }
  public void setBio(String bio) { this.bio = bio; }
}
