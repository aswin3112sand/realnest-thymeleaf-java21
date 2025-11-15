package com.realnest.web;

import com.realnest.enquiry.EnquiryForm;
import com.realnest.enquiry.EnquiryService;
import com.realnest.enquiry.EnquiryStatus;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class EnquiryController {

  private final EnquiryService enquiryService;
  private final PropertyService propertyService;

  public EnquiryController(EnquiryService enquiryService, PropertyService propertyService) {
    this.enquiryService = enquiryService;
    this.propertyService = propertyService;
  }

  @PostMapping("/properties/{propertyId}/enquiries")
  public String submit(
      @PathVariable Long propertyId,
      @ModelAttribute("enquiryForm") @Valid EnquiryForm form,
      BindingResult bindingResult,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    Property property = propertyService.findById(propertyId);
    if (!property.isApproved()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing unavailable");
    }
    if (bindingResult.hasErrors()) {
      boolean canManage = isOwner(authentication, property) || hasAdmin(authentication);
      model.addAttribute("property", property);
      model.addAttribute("canManage", canManage);
      model.addAttribute("enquiryForm", form);
      return "properties/detail";
    }
    enquiryService.submit(propertyId, form);
    redirectAttributes.addFlashAttribute(
        "successMessage", "Thanks! Your enquiry has been sent to the listing agent.");
    return "redirect:/properties/" + propertyId;
  }

  @GetMapping("/admin/enquiries")
  @PreAuthorize("hasRole('ADMIN')")
  public String list(
      @PageableDefault(size = 20) Pageable pageable,
      Model model,
      @RequestParam(name = "status", required = false) EnquiryStatus status) {
    model.addAttribute("page", enquiryService.list(status, pageable));
    model.addAttribute("statuses", EnquiryStatus.values());
    model.addAttribute("selectedStatus", status);
    model.addAttribute(
        "searchParams",
        java.util.Map.of(
            "location",
            "",
            "type",
            "",
            "min",
            "",
            "max",
            "",
            "status",
            status != null ? status.name() : ""));
    return "admin/enquiries";
  }

  @PostMapping("/admin/enquiries/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public String updateStatus(
      @PathVariable Long id,
      @RequestParam("status") EnquiryStatus status,
      RedirectAttributes redirectAttributes) {
    enquiryService.updateStatus(id, status);
    redirectAttributes.addFlashAttribute("successMessage", "Enquiry status updated.");
    return "redirect:/admin/enquiries";
  }

  private boolean isOwner(Authentication authentication, Property property) {
    return authentication != null
        && property.getOwner() != null
        && authentication.getName().equals(property.getOwner().getEmail());
  }

  private boolean hasAdmin(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
  }
}
