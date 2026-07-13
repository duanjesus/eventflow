package com.pulsequeue.repository;

import com.pulsequeue.entity.InstitutionMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionMappingRepository extends JpaRepository<InstitutionMapping, Long> {

    Optional<InstitutionMapping> findByInstitutionId(Long institutionId);

    List<InstitutionMapping> findAllByOrderByInstitutionIdAsc();
}
