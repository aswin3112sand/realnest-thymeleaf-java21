package com.realnest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.realnest.property.Property;
import com.realnest.property.PropertyRepository;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import com.realnest.user.Role;
import com.realnest.user.User;
import com.realnest.user.UserRepository;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

  @Mock private PropertyRepository propertyRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private PropertyService propertyService;

  @Test
  void createAssignsOwnerAndImage() {
    User owner = owner();
    when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
    when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Property request = property("Modern Loft");

    Property saved = propertyService.create(request, owner.getEmail(), "https://img");

    assertThat(saved.getOwner()).isEqualTo(owner);
    assertThat(saved.getImageUrl()).isEqualTo("https://img");
    assertThat(saved.isApproved()).isFalse();
    verify(propertyRepository).save(request);
  }

  @Test
  void approveMarksListingAsApproved() {
    Property property = property("Lake House");
    property.setId(44L);
    when(propertyRepository.findById(44L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(property)).thenReturn(property);

    Property approved = propertyService.approve(44L);

    assertThat(approved.isApproved()).isTrue();
  }

  @Test
  void deleteThrowsWhenIdMissing() {
    when(propertyRepository.findById(9L)).thenReturn(Optional.empty());
    assertThrows(NoSuchElementException.class, () -> propertyService.delete(9L));
  }

  private Property property(String title) {
    Property property = new Property();
    property.setTitle(title);
    property.setDescription("desc");
    property.setLocation("Chennai");
    property.setPrice(BigDecimal.valueOf(100));
    property.setType(PropertyType.SALE);
    return property;
  }

  private User owner() {
    User owner = new User();
    owner.setId(7L);
    owner.setEmail("owner@realnest.com");
    owner.setName("Owner");
    owner.setPassword("secret");
    owner.setRole(Role.CUSTOMER);
    return owner;
  }
}

