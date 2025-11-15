package com.realnest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.realnest.config.SecurityConfig;
import com.realnest.property.ImageStorageService;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import com.realnest.property.PropertyType;
import com.realnest.user.User;
import com.realnest.user.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;

@WebMvcTest(com.realnest.web.HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PropertyService propertyService;
  @MockBean private ImageStorageService imageStorageService;
  @MockBean private UserRepository userRepository;

  @Test
  void propertiesPageLoadsWithResults() throws Exception {
    Property property = property();
    Page<Property> page = new PageImpl<>(java.util.List.of(property), PageRequest.of(0, 12), 1);
    when(propertyService.search(any(), any(), any(), any(), any())).thenReturn(page);

    mockMvc
        .perform(get("/properties"))
        .andExpect(status().isOk())
        .andExpect(view().name("properties/list"))
        .andExpect(model().attributeExists("page", "types", "resultCount"));
  }

  @Test
  @WithMockUser
  void detailPageAccessibleForApprovedListing() throws Exception {
    Property property = property();
    property.setApproved(true);
    User owner = new User();
    owner.setEmail("owner@demo.com");
    property.setOwner(owner);
    when(propertyService.findById(3L)).thenReturn(property);

    mockMvc
        .perform(get("/properties/3").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("properties/detail"))
        .andExpect(model().attributeExists("property", "enquiryForm"));
  }

  private Property property() {
    Property property = new Property();
    property.setTitle("Beach House");
    property.setDescription("desc");
    property.setLocation("Goa");
    property.setType(PropertyType.SALE);
    property.setPrice(BigDecimal.valueOf(2000000));
    return property;
  }
}
