package com.realnest.api;

import com.realnest.property.Property;
import com.realnest.property.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PropertyRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String location,
    @NotNull PropertyType type,
    @NotNull @Positive BigDecimal price,
    String imageUrl) {

  public Property toEntity() {
    Property property = new Property();
    property.setTitle(title);
    property.setDescription(description);
    property.setLocation(location);
    property.setType(type);
    property.setPrice(price);
    property.setImageUrl(imageUrl);
    return property;
  }
}
