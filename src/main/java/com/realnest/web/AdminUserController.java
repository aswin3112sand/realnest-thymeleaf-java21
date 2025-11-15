package com.realnest.web;

import com.realnest.user.AdminUserForm;
import com.realnest.user.Role;
import com.realnest.user.User;
import com.realnest.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public String users(Model model) {
    model.addAttribute("users", userService.findAll());
    return "admin/users";
  }

  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable Long id, Model model) {
    User user = userService.findById(id);
    AdminUserForm form = new AdminUserForm();
    form.setName(user.getName());
    form.setPhone(user.getPhone());
    form.setRole(user.getRole());
    form.setEmail(user.getEmail());
    model.addAttribute("user", form);
    model.addAttribute("roles", Role.values());
    return "admin/user-edit";
  }

  @PostMapping("/{id}/edit")
  public String update(
      @PathVariable Long id,
      @ModelAttribute("user") @Valid AdminUserForm form,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("roles", Role.values());
      return "admin/user-edit";
    }
    User existing = userService.findById(id);
    existing.setName(form.getName());
    existing.setPhone(form.getPhone());
    existing.setRole(form.getRole());
    userService.update(existing);
    redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
    return "redirect:/admin/users";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirects) {
    userService.delete(id);
    redirects.addFlashAttribute("successMessage", "User deleted.");
    return "redirect:/admin/users";
  }
}
