package com.realnest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.realnest.customer.Customer;
import com.realnest.customer.CustomerRepository;
import com.realnest.customer.CustomerService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;
  @InjectMocks private CustomerService customerService;

  @Test
  void getAllReturnsCustomers() {
    when(customerRepository.findAll()).thenReturn(List.of(customer("Riya"), customer("Arun")));
    List<Customer> customers = customerService.getAll();
    assertThat(customers).hasSize(2).extracting(Customer::getName).contains("Riya", "Arun");
  }

  @Test
  void updateCopiesIncomingFields() {
    Customer existing = customer("Priya");
    existing.setId(9L);
    when(customerRepository.findById(9L)).thenReturn(Optional.of(existing));
    when(customerRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

    Customer updated = customer("Priya Shah");
    updated.setEmail("new@mail.com");
    updated.setPhone("12345678");

    Customer result = customerService.update(9L, updated);

    assertThat(result.getName()).isEqualTo("Priya Shah");
    assertThat(result.getEmail()).isEqualTo("new@mail.com");
    assertThat(result.getPhone()).isEqualTo("12345678");
  }

  @Test
  void deleteDelegatesToRepository() {
    customerService.delete(5L);
    verify(customerRepository, times(1)).deleteById(5L);
  }

  @Test
  void savePersistsCustomer() {
    Customer request = customer("Raman");
    when(customerRepository.save(any(Customer.class))).thenReturn(request);
    Customer saved = customerService.save(request);
    assertThat(saved).isSameAs(request);
  }

  private Customer customer(String name) {
    Customer customer = new Customer();
    customer.setName(name);
    customer.setEmail(name.toLowerCase() + "@demo.com");
    customer.setPhone("9876512345");
    return customer;
  }
}

