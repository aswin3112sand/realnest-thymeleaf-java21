package com.realnest.enquiry;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {
  List<Enquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

  org.springframework.data.domain.Page<Enquiry> findByStatus(
      EnquiryStatus status, org.springframework.data.domain.Pageable pageable);
}
