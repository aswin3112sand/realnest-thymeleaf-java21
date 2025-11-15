package com.realnest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.realnest.config.SecurityConfig;
import com.realnest.enquiry.Enquiry;
import com.realnest.enquiry.EnquiryService;
import com.realnest.enquiry.EnquiryStatus;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import com.realnest.user.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(com.realnest.web.EnquiryController.class)
@Import(SecurityConfig.class)
class EnquiryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EnquiryService enquiryService;
  @MockBean private PropertyService propertyService;
  @MockBean private UserRepository userRepository;

  @Test
  void submitStoresEnquiry() throws Exception {
    Property property = property();
    property.setApproved(true);
    when(propertyService.findById(12L)).thenReturn(property);

    mockMvc
        .perform(
            post("/properties/12/enquiries")
                .with(csrf())
                .param("name", "Vishal")
                .param("email", "vishal@demo.com")
                .param("phone", "9999999999")
                .param("message", "Need more info"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/properties/12"));

    verify(enquiryService).submit(eq(12L), any());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminListShowsPage() throws Exception {
    Enquiry enquiry = new Enquiry();
    enquiry.setProperty(property());
    enquiry.setName("Buyer");
    enquiry.setEmail("buyer@demo.com");
    enquiry.setPhone("9000000000");
    enquiry.setMessage("Interested");

    when(enquiryService.list(any(), any()))
        .thenReturn(new PageImpl<>(java.util.List.of(enquiry), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/admin/enquiries"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/enquiries"))
        .andExpect(model().attributeExists("page", "statuses"));
  }

  private Property property() {
    Property property = new Property();
    property.setId(12L);
    property.setTitle("Premium Loft");
    property.setDescription("desc");
    property.setLocation("Chennai");
    property.setType(PropertyType.SALE);
    property.setPrice(BigDecimal.valueOf(4000000));
    return property;
  }
}
