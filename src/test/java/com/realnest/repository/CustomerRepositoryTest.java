package com.realnest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.realnest.customer.Customer;
import com.realnest.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class CustomerRepositoryTest {

  @Autowired private CustomerRepository customerRepository;
  @Autowired private TestEntityManager entityManager;

  @Test
  void saveAndFindCustomers() {
    Customer customer = new Customer();
    customer.setName("Lakshmi");
    customer.setEmail("lakshmi@demo.com");
    customer.setPhone("1234567890");

    entityManager.persist(customer);
    entityManager.flush();

    assertThat(customerRepository.findAll()).hasSize(1).first().extracting(Customer::getEmail).isEqualTo("lakshmi@demo.com");
  }
}

