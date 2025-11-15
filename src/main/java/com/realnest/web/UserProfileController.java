package com.realnest.web;

import com.realnest.user.User;
import com.realnest.user.UserProfileForm;
import com.realnest.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/profile")
@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
public class UserProfileController {

  private final UserService userService;

  public UserProfileController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public String profile(Authentication authentication, Model model) {
    User current = userService.findByEmail(authentication.getName());
    if (!model.containsAttribute("user")) {
      UserProfileForm form = new UserProfileForm();
      form.setName(current.getName());
      form.setPhone(current.getPhone());
      form.setEmail(current.getEmail());
      model.addAttribute("user", form);
    }
    model.addAttribute("email", current.getEmail());
    return "user/profile";
  }

  @PostMapping
  public String updateProfile(
      @ModelAttribute("user") @Valid UserProfileForm form,
      BindingResult bindingResult,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    User current = userService.findByEmail(authentication.getName());
    if (bindingResult.hasErrors()) {
      model.addAttribute("user", form);
      model.addAttribute("email", current.getEmail());
      return "user/profile";
    }
    current.setName(form.getName());
    current.setPhone(form.getPhone());
    userService.update(current);
    redirectAttributes.addFlashAttribute("successMessage", "Profile updated");
    return "redirect:/user/profile";
  }
}
