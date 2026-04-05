package com.toto.backend.repository;

import com.toto.backend.entity.UpdateRequest;
import com.toto.backend.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateRequestRepository extends JpaRepository<UpdateRequest, Long> {
    List<UpdateRequest> findByEmployeeIdAndStatusOrderByCreatedAtDesc(String employeeId, Status status);
    List<UpdateRequest> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    boolean existsByEmployeeIdAndFieldLabelAndStatus(String employeeId, String fieldLabel, Status status);
    List<UpdateRequest> findByStatusOrderByCreatedAtDesc(Status status);
    List<UpdateRequest> findAllByOrderByCreatedAtDesc();
}
