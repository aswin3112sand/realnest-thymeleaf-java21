package com.realnest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.realnest.config.SecurityConfig;
import com.realnest.customer.Customer;
import com.realnest.customer.CustomerService;
import com.realnest.property.ImageStorageService;
import com.realnest.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(com.realnest.web.CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CustomerService customerService;
  @MockBean private ImageStorageService imageStorageService;
  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void listDisplaysCustomers() throws Exception {
    when(customerService.getAll()).thenReturn(List.of(customer()));
    mockMvc
        .perform(get("/admin/customers"))
        .andExpect(status().isOk())
        .andExpect(view().name("customers/list"))
        .andExpect(model().attributeExists("customers"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createCustomerPersistsAndRedirects() throws Exception {
    MockMultipartFile photo =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "demo".getBytes());
    when(imageStorageService.store(any(), any())).thenReturn(Optional.of("/uploads/photo.jpg"));

    mockMvc
        .perform(
            multipart("/admin/customers")
                .file(photo)
                .with(csrf())
                .param("name", "Riya")
                .param("email", "riya@demo.com")
                .param("phone", "9999999999"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/customers"));

    verify(customerService).save(any(Customer.class));
  }

  private Customer customer() {
    Customer customer = new Customer();
    customer.setId(1L);
    customer.setName("Riya");
    customer.setEmail("riya@demo.com");
    customer.setPhone("9876543210");
    return customer;
  }
}
