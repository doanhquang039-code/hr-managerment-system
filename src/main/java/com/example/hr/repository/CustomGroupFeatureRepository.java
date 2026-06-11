package com.example.hr.repository;

import com.example.hr.models.CustomGroupFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomGroupFeatureRepository extends JpaRepository<CustomGroupFeature, Integer> {
    Optional<CustomGroupFeature> findByName(String name);
}
