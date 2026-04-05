package com.toto.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;

    @Column(name = "field_key", length = 100)
    private String fieldKey;

    @Column(name = "dropdown_values", columnDefinition = "TEXT")
    private String dropdownValues;

    // 1 = optional (không bắt buộc file), 0 = required
    @Column(name = "is_optional_upload")
    private Integer isOptionalUpload = 0;

    // 1 = locked (không cho sửa)
    @Column(name = "is_locked")
    private Integer isLocked = 0;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
