package com.toto.backend.repository;

import com.toto.backend.entity.LookupCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LookupCategoryRepository extends JpaRepository<LookupCategory, Long> {
    List<LookupCategory> findByStatusOrderByCategoryNameAsc(Integer status);
    List<LookupCategory> findByCategoryNameAndStatusOrderByPeriodDesc(String categoryName, Integer status);
}
