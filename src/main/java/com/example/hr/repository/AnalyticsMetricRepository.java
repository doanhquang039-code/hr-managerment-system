package com.example.hr.repository;

import com.example.hr.models.AnalyticsMetric;
import com.example.hr.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticsMetricRepository extends JpaRepository<AnalyticsMetric, Long> {
    
    List<AnalyticsMetric> findByMetricTypeAndPeriodOrderByDateDesc(String metricType, String period);
    
    List<AnalyticsMetric> findByMetricTypeAndDateBetween(String metricType, LocalDate startDate, LocalDate endDate);
    
    List<AnalyticsMetric> findByDepartmentAndMetricTypeAndDateBetween(
        Department department, String metricType, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT am FROM AnalyticsMetric am WHERE am.metricType = :metricType " +
           "AND am.date >= :startDate AND am.date <= :endDate ORDER BY am.date ASC")
    List<AnalyticsMetric> findMetricsByTypeAndDateRange(
        @Param("metricType") String metricType,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}


