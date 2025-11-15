package com.realnest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.realnest.config.SecurityConfig;
import com.realnest.property.ImageStorageService;
import com.realnest.property.PropertyService;
import com.realnest.user.UserRepository;
import com.realnest.web.AdminController;
import com.realnest.web.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {HomeController.class, AdminController.class})
@Import(SecurityConfig.class)
class SecurityAccessTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PropertyService propertyService;
  @MockBean private ImageStorageService imageStorageService;
  @MockBean private com.realnest.customer.CustomerService customerService;
  @MockBean private com.realnest.enquiry.EnquiryService enquiryService;
  @MockBean private UserRepository userRepository;

  @Test
  void dashboardRequiresAuth() throws Exception {
    mockMvc
        .perform(get("/dashboard/me"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminApprovalsLoadsForAdmin() throws Exception {
    org.mockito.Mockito.when(propertyService.pending(org.mockito.ArgumentMatchers.any(Pageable.class)))
        .thenReturn(Page.empty());

    mockMvc.perform(get("/admin/approvals")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "CUSTOMER")
  void adminApprovalsForbiddenForCustomer() throws Exception {
    mockMvc.perform(get("/admin/approvals")).andExpect(status().isForbidden());
  }
}
