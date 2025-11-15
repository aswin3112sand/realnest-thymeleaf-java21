package com.realnest.enquiry;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EnquiryForm {

  @NotBlank(message = "Please share your name")
  private String name;

  @NotBlank(message = "We need a valid email")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Phone is required")
  @Size(min = 7, max = 20, message = "Phone should be between 7 and 20 characters")
  private String phone;

  @NotBlank(message = "Message cannot be empty")
  @Size(max = 1000, message = "Message cannot exceed 1000 characters")
  private String message;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

