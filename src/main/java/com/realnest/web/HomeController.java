package com.realnest.web;

import com.realnest.enquiry.EnquiryForm;
import com.realnest.property.ImageStorageService;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import com.realnest.user.User;
import com.realnest.user.UserRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

  private final PropertyService propertyService;
  private final ImageStorageService imageStorageService;
  private final UserRepository userRepository;

  public HomeController(
      PropertyService propertyService,
      ImageStorageService imageStorageService,
      UserRepository userRepository) {
    this.propertyService = propertyService;
    this.imageStorageService = imageStorageService;
    this.userRepository = userRepository;
  }

  @GetMapping("/")
  public String rootRedirect() {
    return "redirect:/properties";
  }

  @GetMapping("/properties")
  public String list(
      @RequestParam(required = false) String location,
      @RequestParam(required = false) PropertyType type,
      @RequestParam(required = false) String min,
      @RequestParam(required = false) String max,
      @PageableDefault(size = 12) Pageable pageable,
      Authentication authentication,
      Model model) {
    BigDecimal minPrice = parseDecimal(min);
    BigDecimal maxPrice = parseDecimal(max);
    String locationFilter = normalize(location);
    Page<Property> page =
        propertyService.search(locationFilter, type, minPrice, maxPrice, pageable);
    Map<String, String> searchParams = buildSearchParams(location, type, min, max);
    String currentUserEmail = authentication != null ? authentication.getName() : null;
    model.addAttribute("page", page);
    model.addAttribute("types", PropertyType.values());
    model.addAttribute("location", location);
    model.addAttribute("type", type);
    model.addAttribute("min", min);
    model.addAttribute("max", max);
    model.addAttribute("resultCount", page.getTotalElements());
    model.addAttribute("searchParams", searchParams);
    model.addAttribute("currentUserEmail", currentUserEmail);
    return "properties/list";
  }

  @GetMapping("/properties/new")
  @PreAuthorize("isAuthenticated()")
  public String form(Model model) {
    if (!model.containsAttribute("property")) {
      model.addAttribute("property", new Property());
    }
    model.addAttribute("types", PropertyType.values());
    model.addAttribute("editing", false);
    return "properties/form";
  }

  @PostMapping("/properties")
  @PreAuthorize("isAuthenticated()")
  public String create(
      @ModelAttribute("property") @Valid Property property,
      BindingResult bindingResult,
      @RequestParam(value = "image", required = false) MultipartFile image,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("types", PropertyType.values());
      model.addAttribute("editing", false);
      return "properties/form";
    }
    String ownerEmail = authentication.getName();
    String imageUrl = imageStorageService.store(image, "properties").orElse(null);
    propertyService.create(property, ownerEmail, imageUrl);
    redirectAttributes.addFlashAttribute(
        "successMessage", "Property submitted! Awaiting admin approval.");
    return "redirect:/dashboard/me";
  }

  @GetMapping("/properties/{id}/edit")
  @PreAuthorize("isAuthenticated()")
  public String edit(@PathVariable Long id, Authentication authentication, Model model) {
    Long ownerId = currentUserId(authentication);
    Property property = propertyService.getOwned(id, ownerId);
    if (!model.containsAttribute("property")) {
      model.addAttribute("property", property);
    }
    model.addAttribute("types", PropertyType.values());
    model.addAttribute("editing", true);
    return "properties/form";
  }

  @PostMapping("/properties/{id}")
  @PreAuthorize("isAuthenticated()")
  public String update(
      @PathVariable Long id,
      @ModelAttribute("property") @Valid Property property,
      BindingResult bindingResult,
      @RequestParam(value = "image", required = false) MultipartFile image,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("types", PropertyType.values());
      model.addAttribute("editing", true);
      return "properties/form";
    }
    Long ownerId = currentUserId(authentication);
    imageStorageService.store(image, "properties").ifPresent(property::setImageUrl);
    propertyService.updateOwned(id, ownerId, property);
    redirectAttributes.addFlashAttribute("successMessage", "Listing updated successfully.");
    return "redirect:/dashboard/me";
  }

  @PostMapping("/properties/{id}/delete")
  @PreAuthorize("isAuthenticated()")
  public String delete(
      @PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
    Long ownerId = currentUserId(authentication);
    propertyService.deleteOwned(id, ownerId);
    redirectAttributes.addFlashAttribute("successMessage", "Listing deleted.");
    return "redirect:/dashboard/me";
  }

  @GetMapping("/dashboard/me")
  @PreAuthorize("isAuthenticated()")
  public String myListings(
      @PageableDefault(size = 12) Pageable pageable,
      Authentication authentication,
      Model model) {
    Long ownerId = currentUserId(authentication);
    Page<Property> page = propertyService.ownerListings(ownerId, pageable);
    model.addAttribute("page", page);
    model.addAttribute("searchParams", blankParams());
    return "dashboard/my-listings";
  }

  @GetMapping("/properties/{id}")
  public String detail(@PathVariable Long id, Authentication authentication, Model model) {
    Property property = propertyService.findById(id);
    boolean approved = property.isApproved();
    boolean isOwner = isOwner(authentication, property);
    boolean isAdmin = hasAdminRole(authentication);
    if (!approved && !isOwner && !isAdmin) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing unavailable");
    }
    if (!model.containsAttribute("enquiryForm")) {
      model.addAttribute("enquiryForm", new EnquiryForm());
    }
    model.addAttribute("property", property);
    model.addAttribute("canManage", isOwner || isAdmin);
    return "properties/detail";
  }

  private BigDecimal parseDecimal(String value) {
    try {
      return value == null || value.isBlank() ? null : new BigDecimal(value);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private Map<String, String> buildSearchParams(
      String location, PropertyType type, String min, String max) {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("location", location != null ? location : "");
    params.put("type", type != null ? type.name() : "");
    params.put("min", min != null ? min : "");
    params.put("max", max != null ? max : "");
    params.put("status", "");
    return params;
  }

  private Map<String, String> blankParams() {
    return Map.of("location", "", "type", "", "min", "", "max", "", "status", "");
  }

  private Long currentUserId(Authentication authentication) {
    return userRepository
        .findByEmail(authentication.getName())
        .map(User::getId)
        .orElseThrow(() -> new IllegalStateException("User not found"));
  }

  private boolean isOwner(Authentication authentication, Property property) {
    if (authentication == null || property.getOwner() == null) {
      return false;
    }
    return authentication.getName().equals(property.getOwner().getEmail());
  }

  private boolean hasAdminRole(Authentication authentication) {
    if (authentication == null) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_ADMIN"::equals);
  }
}
