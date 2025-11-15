package com.realnest.property;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyRepository extends JpaRepository<Property, Long> {

  @Query("""
      SELECT p FROM Property p
      WHERE (:loc IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :loc, '%')))
      AND (:type IS NULL OR p.type = :type)
      AND (:min IS NULL OR p.price >= :min)
      AND (:max IS NULL OR p.price <= :max)
      AND p.approved = true
      """)
  Page<Property> search(
      @Param("loc") String loc,
      @Param("type") PropertyType type,
      @Param("min") BigDecimal min,
      @Param("max") BigDecimal max,
      Pageable pageable);

  Page<Property> findByOwner_Id(Long ownerId, Pageable pageable);

  Page<Property> findByApprovedFalse(Pageable pageable);

  Optional<Property> findByIdAndOwner_Id(Long id, Long ownerId);

  boolean existsByIdAndOwner_Id(Long id, Long ownerId);

  long countByApprovedTrue();

  long countByApprovedFalse();
}
