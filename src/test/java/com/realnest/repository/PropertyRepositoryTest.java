package com.realnest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.realnest.property.Property;
import com.realnest.property.PropertyRepository;
import com.realnest.property.PropertyType;
import com.realnest.user.Role;
import com.realnest.user.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class PropertyRepositoryTest {

  @Autowired private PropertyRepository propertyRepository;
  @Autowired private TestEntityManager entityManager;

  private User owner;

  @BeforeEach
  void setUp() {
    propertyRepository.deleteAll();
    owner = entityManager.persist(user("owner@demo.com"));
    persistProperty("Beach House", "Goa", PropertyType.SALE, 25000000, true);
    persistProperty("City Condo", "Chennai", PropertyType.RENT, 80000, true);
    persistProperty("Luxury Villa", "Chennai", PropertyType.SALE, 32000000, true);
    persistProperty("Draft Listing", "Chennai", PropertyType.SALE, 19000000, false);
    entityManager.flush();
  }

  @Test
  void filtersByLocationCaseInsensitive() {
    Page<Property> results =
        propertyRepository.search("chen", null, null, null, PageRequest.of(0, 10));
    assertThat(results).hasSize(2);
  }

  @Test
  void filtersByTypeAndPriceRange() {
    Page<Property> results =
        propertyRepository.search(
            null,
            PropertyType.SALE,
            BigDecimal.valueOf(30000000),
            BigDecimal.valueOf(33000000),
            PageRequest.of(0, 10));
    assertThat(results).hasSize(1);
    assertThat(results.getContent().get(0).getTitle()).isEqualTo("Luxury Villa");
  }

  @Test
  void excludesUnapprovedListings() {
    Page<Property> results =
        propertyRepository.search("draft", null, null, null, PageRequest.of(0, 10));
    assertThat(results).isEmpty();
  }

  private void persistProperty(
      String title, String location, PropertyType type, double price, boolean approved) {
    Property property = new Property();
    property.setTitle(title);
    property.setDescription("desc");
    property.setLocation(location);
    property.setType(type);
    property.setPrice(BigDecimal.valueOf(price));
    property.setOwner(owner);
    property.setApproved(approved);
    entityManager.persist(property);
  }

  private User user(String email) {
    User user = new User();
    user.setName("Owner");
    user.setEmail(email);
    user.setPassword("password");
    user.setRole(Role.CUSTOMER);
    return user;
  }
}

