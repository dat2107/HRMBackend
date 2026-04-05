package com.toto.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lookup_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "file_link", columnDefinition = "TEXT")
    private String fileLink;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "period")
    private LocalDate period;

    // NONE, CONFIRM, CUSTOM
    @Column(name = "feedback_type", length = 20)
    private String feedbackType = "NONE";

    @Column(name = "custom_options", columnDefinition = "TEXT")
    private String customOptions;

    @Column(name = "is_priority")
    private Boolean isPriority = false;

    // JSON data parsed from uploaded Excel: {"headers":["Col1","Col2"...],"rows":[{"Col1":"val",...},...]}
    @Column(name = "data_json", columnDefinition = "LONGTEXT")
    private String dataJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
