package com.example.hr.repository;

import com.example.hr.models.TrainingVideo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrainingVideoRepository extends JpaRepository<TrainingVideo, Integer> {

    // Method 1
    List<TrainingVideo> findByTitleContainingIgnoreCase(String title);

    // Method 2 ✅ — VideoService dùng ở if(hasKeyword)
    List<TrainingVideo> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
        String title, String category, Sort sort);

    // Method 3
    List<TrainingVideo> findByCategory(String category);

    // Method 4 ✅ — VideoService dùng ở if(hasKeyword && hasCategory)
    List<TrainingVideo> findByTitleContainingIgnoreCaseAndCategory(
        String title, String category);
}