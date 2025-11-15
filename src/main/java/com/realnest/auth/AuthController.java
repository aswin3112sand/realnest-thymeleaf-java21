package com.realnest.auth;

import com.realnest.user.Role;
import com.realnest.user.User;
import com.realnest.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

  private final UserRepository repository;
  private final PasswordEncoder encoder;

  public AuthController(UserRepository repository, PasswordEncoder encoder) {
    this.repository = repository;
    this.encoder = encoder;
  }

  @GetMapping("/login")
  public String login() {
    return "auth/login";
  }

  @GetMapping("/register")
  public String registerForm(Model model) {
    if (!model.containsAttribute("user")) {
      model.addAttribute("user", new User());
    }
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
    if (repository.existsByEmail(user.getEmail())) {
      bindingResult.rejectValue("email", "exists", "Email already registered");
    }
    if (bindingResult.hasErrors()) {
      return "auth/register";
    }
    user.setPassword(encoder.encode(user.getPassword()));
    user.setRole(Role.CUSTOMER);
    repository.save(user);
    return "redirect:/login?success";
  }
}
