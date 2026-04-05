package com.toto.backend.repository;

import com.toto.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);

    @Modifying
    @Query("UPDATE Account a SET a.tokenVersion = a.tokenVersion + 1 WHERE a.employeeId = :employeeId")
    void incrementTokenVersion(String employeeId);

    @Modifying
    @Query("UPDATE Account a SET a.failedAttempts = 0, a.isLocked = false, a.lockedUntil = null WHERE a.employeeId = :employeeId")
    void resetFailedAttempts(String employeeId);
}
