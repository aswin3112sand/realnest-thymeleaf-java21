package com.realnest.api;

import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyRestController {

  private final PropertyService propertyService;

  public PropertyRestController(PropertyService propertyService) {
    this.propertyService = propertyService;
  }

  @GetMapping
  public Page<PropertyResponse> search(
      @RequestParam(required = false) String location,
      @RequestParam(required = false) PropertyType type,
      @RequestParam(required = false) String min,
      @RequestParam(required = false) String max,
      @PageableDefault(size = 12) Pageable pageable) {
    Page<Property> page =
        propertyService.search(normalize(location), type, parse(min), parse(max), pageable);
    return page.map(PropertyResponse::from);
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PropertyResponse> create(
      @Valid @RequestBody PropertyRequest request, Authentication authentication) {
    Property property =
        propertyService.create(request.toEntity(), authentication.getName(), request.imageUrl());
    return ResponseEntity.status(201).body(PropertyResponse.from(property));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PropertyResponse> approve(@PathVariable Long id) {
    Property property = propertyService.approve(id);
    return ResponseEntity.ok(PropertyResponse.from(property));
  }

  private BigDecimal parse(String value) {
    try {
      return value == null || value.isBlank() ? null : new BigDecimal(value);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
