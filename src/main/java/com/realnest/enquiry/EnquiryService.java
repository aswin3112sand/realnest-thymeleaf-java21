package com.realnest.enquiry;

import com.realnest.property.Property;
import com.realnest.property.PropertyService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnquiryService {

  private final EnquiryRepository repository;
  private final PropertyService propertyService;

  public EnquiryService(EnquiryRepository repository, PropertyService propertyService) {
    this.repository = repository;
    this.propertyService = propertyService;
  }

  @Transactional
  public Enquiry submit(Long propertyId, EnquiryForm form) {
    Property property = propertyService.findById(propertyId);
    if (!property.isApproved()) {
      throw new IllegalStateException("Property is not open for enquiries");
    }
    Enquiry enquiry = new Enquiry();
    enquiry.setProperty(property);
    enquiry.setName(form.getName());
    enquiry.setEmail(form.getEmail());
    enquiry.setPhone(form.getPhone());
    enquiry.setMessage(form.getMessage());
    enquiry.setStatus(EnquiryStatus.NEW);
    return repository.save(enquiry);
  }

  public Page<Enquiry> list(EnquiryStatus status, Pageable pageable) {
    if (status != null) {
      return repository.findByStatus(status, pageable);
    }
    return repository.findAll(pageable);
  }

  @Transactional
  public void updateStatus(Long id, EnquiryStatus status) {
    Enquiry enquiry =
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Enquiry not found"));
    enquiry.setStatus(status);
    repository.save(enquiry);
  }

  public long totalCount() {
    return repository.count();
  }

  public List<Enquiry> recent(int limit) {
    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByOrderByCreatedAtDesc(pageable);
  }
}
