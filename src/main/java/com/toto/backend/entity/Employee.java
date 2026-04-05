package com.toto.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "position_code", length = 50)
    private String positionCode;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "personal_email", length = 200)
    private String personalEmail;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "ethnicity", length = 50)
    private String ethnicity;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "temp_province", length = 100)
    private String tempProvince;

    @Column(name = "temp_district", length = 100)
    private String tempDistrict;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "insurance_code", length = 20)
    private String insuranceCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
