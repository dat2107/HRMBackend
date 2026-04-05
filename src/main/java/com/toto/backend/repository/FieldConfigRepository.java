package com.toto.backend.repository;

import com.toto.backend.entity.FieldConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldConfigRepository extends JpaRepository<FieldConfig, Long> {
    List<FieldConfig> findAllByOrderByDisplayOrderAsc();
    Optional<FieldConfig> findByFieldNameIgnoreCase(String fieldName);
    Optional<FieldConfig> findByFieldKeyIgnoreCase(String fieldKey);
}
