package com.example.hr.repository;

import com.example.hr.models.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    
    List<PublicHoliday> findByYearAndIsActiveOrderByDateAsc(Integer year, Boolean isActive);
    
    List<PublicHoliday> findByDateBetweenAndIsActive(LocalDate startDate, LocalDate endDate, Boolean isActive);
    
    @Query("SELECT ph FROM PublicHoliday ph WHERE ph.date = :date AND ph.isActive = true")
    List<PublicHoliday> findByDate(@Param("date") LocalDate date);
}


