package com.realnest.user;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }

  public User findById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
  }

  @Transactional
  public User update(User updatedUser) {
    User existing = findById(updatedUser.getId());
    existing.setName(updatedUser.getName());
    existing.setRole(updatedUser.getRole());
    existing.setPhone(updatedUser.getPhone());
    return userRepository.save(existing);
  }

  @Transactional
  public void delete(Long id) {
    userRepository.deleteById(id);
  }

  public User findByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
  }
}

