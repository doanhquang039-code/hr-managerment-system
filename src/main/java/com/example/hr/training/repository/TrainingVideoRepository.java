package com.example.hr.training.repository;

import com.example.hr.training.entity.TrainingVideo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingVideoRepository extends JpaRepository<TrainingVideo, Integer> {

    List<TrainingVideo> findByIsPublishedTrueOrderByIdDesc();

    List<TrainingVideo> findByTitleContainingIgnoreCase(String title);

    List<TrainingVideo> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String title, String category, Sort sort);

    List<TrainingVideo> findByCategory(String category);

    List<TrainingVideo> findByTitleContainingIgnoreCaseAndCategory(String title, String category);

    List<TrainingVideo> findByUploaderId(Integer uploaderId);

    @Query("SELECT DISTINCT v.category FROM TrainingVideo v WHERE v.category IS NOT NULL ORDER BY v.category")
    List<String> findDistinctCategories();

    long countByIsPublishedTrue();
}

