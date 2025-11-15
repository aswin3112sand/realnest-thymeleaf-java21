package com.realnest.api;

import com.realnest.property.Property;
import com.realnest.property.PropertyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PropertyResponse(
    Long id,
    String title,
    String description,
    String location,
    PropertyType type,
    BigDecimal price,
    boolean approved,
    String imageUrl,
    LocalDateTime dateListed) {

  public static PropertyResponse from(Property property) {
    return new PropertyResponse(
        property.getId(),
        property.getTitle(),
        property.getDescription(),
        property.getLocation(),
        property.getType(),
        property.getPrice(),
        property.isApproved(),
        property.getImageUrl(),
        property.getDateListed());
  }
}
