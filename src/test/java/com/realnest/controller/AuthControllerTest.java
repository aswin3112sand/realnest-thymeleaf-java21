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

import com.realnest.auth.AuthController;
import com.realnest.config.SecurityConfig;
import com.realnest.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserRepository userRepository;
  @MockBean private PasswordEncoder passwordEncoder;

  @Test
  void registerFormLoads() throws Exception {
    mockMvc
        .perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/register"))
        .andExpect(model().attributeExists("user"));
  }

  @Test
  void registerSuccessRedirectsToLogin() throws Exception {
    when(userRepository.existsByEmail("new@demo.com")).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("encoded");

    mockMvc
        .perform(
            post("/register")
                .with(csrf())
                .param("name", "New User")
                .param("email", "new@demo.com")
                .param("password", "password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?success"));

    verify(userRepository).save(any());
  }
}
