package com.toto.backend.repository;

import com.toto.backend.entity.AddressConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressConfigRepository extends JpaRepository<AddressConfig, Long> {

    @Query("SELECT DISTINCT a.province FROM AddressConfig a ORDER BY a.province")
    List<String> findAllDistinctProvinces();

    @Query("SELECT DISTINCT a.district FROM AddressConfig a WHERE a.province = :province ORDER BY a.district")
    List<String> findDistrictsByProvince(String province);
}
