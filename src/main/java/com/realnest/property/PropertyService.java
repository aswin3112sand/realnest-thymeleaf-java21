package com.realnest.property;

import com.realnest.user.User;
import com.realnest.user.UserRepository;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyService {

  private final PropertyRepository repository;
  private final UserRepository userRepository;

  public PropertyService(PropertyRepository repository, UserRepository userRepository) {
    this.repository = repository;
    this.userRepository = userRepository;
  }

  @Transactional
  public Property create(Property request, String ownerEmail, String imageUrl) {
    User owner =
        userRepository
            .findByEmail(ownerEmail)
            .orElseThrow(() -> new NoSuchElementException("Owner not found"));
    request.setId(null);
    request.setOwner(owner);
    request.setApproved(false);
    if (imageUrl != null) {
      request.setImageUrl(imageUrl);
    }
    return repository.save(request);
  }

  public Page<Property> search(
      String location, PropertyType type, BigDecimal min, BigDecimal max, Pageable pageable) {
    String normalized = location == null || location.isBlank() ? null : location.trim();
    return repository.search(normalized, type, min, max, pageable);
  }

  public Page<Property> ownerListings(Long ownerId, Pageable pageable) {
    return repository.findByOwner_Id(ownerId, pageable);
  }

  public Page<Property> pending(Pageable pageable) {
    return repository.findByApprovedFalse(pageable);
  }

  public Property findById(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Listing not found"));
  }

  public Property getOwned(Long id, Long ownerId) {
    return repository
        .findByIdAndOwner_Id(id, ownerId)
        .orElseThrow(() -> new NoSuchElementException("Listing not found"));
  }

  @Transactional
  public Property updateOwned(Long id, Long ownerId, Property update) {
    Property existing = getOwned(id, ownerId);
    existing.setTitle(update.getTitle());
    existing.setDescription(update.getDescription());
    existing.setPrice(update.getPrice());
    existing.setType(update.getType());
    existing.setLocation(update.getLocation());
    if (update.getImageUrl() != null) {
      existing.setImageUrl(update.getImageUrl());
    }
    return repository.save(existing);
  }

  @Transactional
  public Property update(Long id, Property update) {
    Property existing = findById(id);
    existing.setTitle(update.getTitle());
    existing.setDescription(update.getDescription());
    existing.setPrice(update.getPrice());
    existing.setType(update.getType());
    existing.setLocation(update.getLocation());
    if (update.getImageUrl() != null) {
      existing.setImageUrl(update.getImageUrl());
    }
    return repository.save(existing);
  }

  @Transactional
  public void deleteOwned(Long id, Long ownerId) {
    if (!repository.existsByIdAndOwner_Id(id, ownerId)) {
      throw new NoSuchElementException("Listing not found");
    }
    repository.deleteById(id);
  }

  @Transactional
  public void delete(Long id) {
    Property property = findById(id);
    repository.delete(property);
  }

  @Transactional
  public Property approve(Long id) {
    Property property =
        repository.findById(id).orElseThrow(() -> new NoSuchElementException("Property not found"));
    property.setApproved(true);
    return repository.save(property);
  }

  public long totalCount() {
    return repository.count();
  }

  public long approvedCount() {
    return repository.countByApprovedTrue();
  }

  public long pendingCount() {
    return repository.countByApprovedFalse();
  }
}
