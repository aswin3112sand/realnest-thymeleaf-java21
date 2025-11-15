package com.realnest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.realnest.enquiry.Enquiry;
import com.realnest.enquiry.EnquiryRepository;
import com.realnest.enquiry.EnquiryStatus;
import com.realnest.property.Property;
import com.realnest.property.PropertyType;
import com.realnest.user.Role;
import com.realnest.user.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class EnquiryRepositoryTest {

  @Autowired private EnquiryRepository enquiryRepository;
  @Autowired private TestEntityManager entityManager;

  @Test
  void findByStatusReturnsMatches() {
    Property property = property();
    Enquiry enquiry = new Enquiry();
    enquiry.setProperty(property);
    enquiry.setName("Buyer");
    enquiry.setEmail("buyer@demo.com");
    enquiry.setPhone("9000000000");
    enquiry.setMessage("Interested");
    enquiry.setStatus(EnquiryStatus.NEW);
    entityManager.persist(enquiry);

    Enquiry contacted = new Enquiry();
    contacted.setProperty(property);
    contacted.setName("Other");
    contacted.setEmail("other@demo.com");
    contacted.setPhone("9333333333");
    contacted.setMessage("Ping");
    contacted.setStatus(EnquiryStatus.CONTACTED);
    entityManager.persist(contacted);
    entityManager.flush();

    Page<Enquiry> results =
        enquiryRepository.findByStatus(EnquiryStatus.NEW, PageRequest.of(0, 10));

    assertThat(results).hasSize(1);
    assertThat(results.getContent().get(0).getEmail()).isEqualTo("buyer@demo.com");
  }

  private Property property() {
    User owner = new User();
    owner.setName("Owner");
    owner.setEmail("owner@demo.com");
    owner.setPassword("secret");
    owner.setRole(Role.CUSTOMER);
    owner = entityManager.persist(owner);

    Property property = new Property();
    property.setTitle("Sample");
    property.setDescription("desc");
    property.setLocation("Goa");
    property.setType(PropertyType.SALE);
    property.setPrice(BigDecimal.valueOf(1000000));
    property.setOwner(owner);
    property.setApproved(true);
    return entityManager.persist(property);
  }
}

