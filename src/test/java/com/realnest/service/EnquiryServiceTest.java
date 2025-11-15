package com.realnest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.realnest.enquiry.Enquiry;
import com.realnest.enquiry.EnquiryForm;
import com.realnest.enquiry.EnquiryRepository;
import com.realnest.enquiry.EnquiryService;
import com.realnest.enquiry.EnquiryStatus;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class EnquiryServiceTest {

  @Mock private EnquiryRepository enquiryRepository;
  @Mock private PropertyService propertyService;
  @InjectMocks private EnquiryService enquiryService;

  @Test
  void submitCreatesNewEnquiry() {
    Property property = property();
    property.setApproved(true);
    when(propertyService.findById(2L)).thenReturn(property);
    when(enquiryRepository.save(any(Enquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    EnquiryForm form = new EnquiryForm();
    form.setName("Nila");
    form.setEmail("nila@demo.com");
    form.setPhone("9888898888");
    form.setMessage("Interested");

    Enquiry enquiry = enquiryService.submit(2L, form);

    assertThat(enquiry.getProperty()).isEqualTo(property);
    assertThat(enquiry.getStatus()).isEqualTo(EnquiryStatus.NEW);
    verify(enquiryRepository).save(any(Enquiry.class));
  }

  @Test
  void listWithStatusFiltersResults() {
    Page<Enquiry> page =
        new PageImpl<>(java.util.List.of(new Enquiry()), PageRequest.of(0, 10), 1);
    when(enquiryRepository.findByStatus(EnquiryStatus.NEW, PageRequest.of(0, 10)))
        .thenReturn(page);

    Page<Enquiry> result = enquiryService.list(EnquiryStatus.NEW, PageRequest.of(0, 10));

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void updateStatusPersistsChange() {
    Enquiry enquiry = new Enquiry();
    enquiry.setStatus(EnquiryStatus.NEW);
    when(enquiryRepository.findById(5L)).thenReturn(Optional.of(enquiry));

    enquiryService.updateStatus(5L, EnquiryStatus.CONTACTED);

    assertThat(enquiry.getStatus()).isEqualTo(EnquiryStatus.CONTACTED);
    verify(enquiryRepository).save(enquiry);
  }

  private Property property() {
    Property property = new Property();
    property.setId(2L);
    property.setTitle("Studio");
    property.setDescription("desc");
    property.setLocation("Goa");
    property.setType(PropertyType.RENT);
    property.setPrice(BigDecimal.valueOf(50000));
    return property;
  }
}

