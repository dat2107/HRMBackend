package com.toto.backend.repository;

import com.toto.backend.entity.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {
    Optional<Confirmation> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}
