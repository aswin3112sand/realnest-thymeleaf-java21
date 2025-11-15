package com.realnest.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.realnest.customer.CustomerService;
import com.realnest.enquiry.EnquiryService;
import com.realnest.property.ImageStorageService;
import com.realnest.property.Property;
import com.realnest.property.PropertyService;
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

@WebMvcTest(com.realnest.web.AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PropertyService propertyService;
  @MockBean private ImageStorageService imageStorageService;
  @MockBean private CustomerService customerService;
  @MockBean private EnquiryService enquiryService;
  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void approvalsPageLoads() throws Exception {
    when(propertyService.pending(any()))
        .thenReturn(new PageImpl<>(java.util.List.of(new Property()), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/admin/approvals"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/approvals"))
        .andExpect(model().attributeExists("page"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void approveEndpointRedirects() throws Exception {
    mockMvc
        .perform(post("/admin/approve/7").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/approvals"));

    verify(propertyService).approve(7L);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void dashboardExposesStats() throws Exception {
    when(propertyService.totalCount()).thenReturn(5L);
    when(propertyService.approvedCount()).thenReturn(3L);
    when(propertyService.pendingCount()).thenReturn(2L);
    when(customerService.totalCount()).thenReturn(4L);
    when(enquiryService.totalCount()).thenReturn(6L);
    when(enquiryService.recent(5)).thenReturn(java.util.List.of());

    mockMvc
        .perform(get("/admin/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/dashboard"))
        .andExpect(model().attributeExists("totalProperties", "totalEnquiries"));
  }
}
