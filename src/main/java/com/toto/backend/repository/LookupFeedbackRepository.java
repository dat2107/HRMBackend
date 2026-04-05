package com.toto.backend.repository;

import com.toto.backend.entity.LookupFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LookupFeedbackRepository extends JpaRepository<LookupFeedback, Long> {
    Optional<LookupFeedback> findByEmployeeIdAndCategoryId(String employeeId, Long categoryId);
}
