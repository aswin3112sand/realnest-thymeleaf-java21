package com.realnest.web;

import com.realnest.customer.Customer;
import com.realnest.customer.CustomerService;
import com.realnest.property.ImageStorageService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

  private final CustomerService customerService;
  private final ImageStorageService imageStorageService;

  public CustomerController(
      CustomerService customerService, ImageStorageService imageStorageService) {
    this.customerService = customerService;
    this.imageStorageService = imageStorageService;
  }

  @GetMapping
  public String listCustomers(Model model) {
    model.addAttribute("customers", customerService.getAll());
    return "customers/list";
  }

  @GetMapping("/new")
  public String newCustomer(Model model) {
    if (!model.containsAttribute("customer")) {
      model.addAttribute("customer", new Customer());
    }
    model.addAttribute("editing", false);
    return "customers/new";
  }

  @PostMapping
  public String saveCustomer(
      @ModelAttribute("customer") @Valid Customer customer,
      BindingResult bindingResult,
      @RequestParam(value = "file", required = false) MultipartFile file,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("editing", false);
      return "customers/new";
    }
    imageStorageService.store(file, "customers").ifPresent(customer::setPhotoPath);
    customerService.save(customer);
    redirectAttributes.addFlashAttribute("successMessage", "Customer added successfully.");
    return "redirect:/admin/customers";
  }

  @GetMapping("/edit/{id}")
  public String editCustomer(@PathVariable Long id, Model model) {
    if (!model.containsAttribute("customer")) {
      Customer customer = customerService.get(id);
      model.addAttribute("customer", customer);
    }
    model.addAttribute("editing", true);
    return "customers/new";
  }

  @PostMapping("/{id}")
  public String updateCustomer(
      @PathVariable Long id,
      @ModelAttribute("customer") @Valid Customer customer,
      BindingResult bindingResult,
      @RequestParam(value = "file", required = false) MultipartFile file,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("editing", true);
      return "customers/new";
    }
    imageStorageService
        .store(file, "customers")
        .ifPresentOrElse(customer::setPhotoPath, () -> retainExistingPhoto(id, customer));
    customerService.update(id, customer);
    redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully.");
    return "redirect:/admin/customers";
  }

  @GetMapping("/delete/{id}")
  public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    customerService.delete(id);
    redirectAttributes.addFlashAttribute("successMessage", "Customer removed.");
    return "redirect:/admin/customers";
  }

  private void retainExistingPhoto(Long id, Customer customer) {
    Customer existing = customerService.get(id);
    customer.setPhotoPath(existing.getPhotoPath());
  }
}
