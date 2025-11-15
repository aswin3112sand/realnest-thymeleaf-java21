package com.realnest.customer;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

  private final CustomerRepository repository;

  public CustomerService(CustomerRepository repository) {
    this.repository = repository;
  }

  public List<Customer> getAll() {
    return repository.findAll();
  }

  public Customer get(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
  }

  public Customer save(Customer customer) {
    return repository.save(customer);
  }

  public Customer update(Long id, Customer updatedCustomer) {
    Customer existing = get(id);
    existing.setName(updatedCustomer.getName());
    existing.setEmail(updatedCustomer.getEmail());
    existing.setPhone(updatedCustomer.getPhone());
    existing.setPhotoPath(updatedCustomer.getPhotoPath());
    return repository.save(existing);
  }

  public void delete(Long id) {
    repository.deleteById(id);
  }

  public long totalCount() {
    return repository.count();
  }
}
