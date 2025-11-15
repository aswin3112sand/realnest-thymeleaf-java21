package com.realnest.web;

import com.realnest.customer.CustomerService;
import com.realnest.enquiry.EnquiryService;
import com.realnest.property.ImageStorageService;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final PropertyService propertyService;
  private final ImageStorageService imageStorageService;
  private final CustomerService customerService;
  private final EnquiryService enquiryService;

  public AdminController(
      PropertyService propertyService,
      ImageStorageService imageStorageService,
      CustomerService customerService,
      EnquiryService enquiryService) {
    this.propertyService = propertyService;
    this.imageStorageService = imageStorageService;
    this.customerService = customerService;
    this.enquiryService = enquiryService;
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    model.addAttribute("totalProperties", propertyService.totalCount());
    model.addAttribute("approvedProperties", propertyService.approvedCount());
    model.addAttribute("pendingProperties", propertyService.pendingCount());
    model.addAttribute("totalCustomers", customerService.totalCount());
    model.addAttribute("totalEnquiries", enquiryService.totalCount());
    model.addAttribute("recentEnquiries", enquiryService.recent(5));
    return "admin/dashboard";
  }

  @GetMapping("/approvals")
  public String approvals(@PageableDefault(size = 20) Pageable pageable, Model model) {
    model.addAttribute("page", propertyService.pending(pageable));
    model.addAttribute("searchParams", blankParams());
    return "admin/approvals";
  }

  @PostMapping("/approve/{id}")
  public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    propertyService.approve(id);
    redirectAttributes.addFlashAttribute("successMessage", "Listing approved");
    return "redirect:/admin/approvals";
  }

  @GetMapping("/properties/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    Property property = propertyService.findById(id);
    if (!model.containsAttribute("property")) {
      model.addAttribute("property", property);
    }
    model.addAttribute("types", PropertyType.values());
    model.addAttribute("editing", true);
    model.addAttribute("adminEdit", true);
    return "properties/form";
  }

  @PostMapping("/properties/{id}")
  public String update(
      @PathVariable Long id,
      @ModelAttribute("property") @Valid Property property,
      BindingResult bindingResult,
      @RequestParam(value = "image", required = false) MultipartFile image,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("types", PropertyType.values());
      model.addAttribute("editing", true);
      model.addAttribute("adminEdit", true);
      return "properties/form";
    }
    imageStorageService.store(image, "properties").ifPresent(property::setImageUrl);
    propertyService.update(id, property);
    redirectAttributes.addFlashAttribute("successMessage", "Listing updated by admin.");
    return "redirect:/admin/approvals";
  }

  @PostMapping("/properties/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    propertyService.delete(id);
    redirectAttributes.addFlashAttribute("successMessage", "Listing deleted.");
    return "redirect:/admin/approvals";
  }

  private Map<String, String> blankParams() {
    return Map.of("location", "", "type", "", "min", "", "max", "", "status", "");
  }
}
