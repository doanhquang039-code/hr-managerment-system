package com.example.hr.system.repository;

import com.example.hr.system.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Integer> {

    Optional<SystemConfiguration> findByConfigKey(String configKey);

    List<SystemConfiguration> findByCategoryOrderByDisplayOrderAsc(String category);

    List<SystemConfiguration> findByIsVisibleTrueOrderByCategoryAscDisplayOrderAsc();

    @Query("SELECT DISTINCT c.category FROM SystemConfiguration c ORDER BY c.category")
    List<String> findAllCategories();

    @Query("SELECT c FROM SystemConfiguration c WHERE c.category = :category AND c.isVisible = true ORDER BY c.displayOrder ASC")
    List<SystemConfiguration> findVisibleByCategory(@Param("category") String category);

    @Query("SELECT c FROM SystemConfiguration c WHERE c.configKey LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<SystemConfiguration> searchConfigurations(@Param("keyword") String keyword);

    boolean existsByConfigKey(String configKey);

    long countByCategory(String category);
    
    // Additional method for SystemConfigurationService
    @Query("SELECT c FROM SystemConfiguration c GROUP BY c.category ORDER BY c.category")
    List<SystemConfiguration> getConfigurationsByCategory();
}


