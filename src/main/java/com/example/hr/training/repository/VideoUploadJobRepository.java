package com.example.hr.training.repository;

import com.example.hr.training.entity.VideoUploadJob;
import com.example.hr.training.entity.VideoUploadJob.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoUploadJobRepository extends JpaRepository<VideoUploadJob, String> {

    List<VideoUploadJob> findByUploaderIdOrderByCreatedAtDesc(Integer uploaderId);

    /** Tìm các job đã hoàn thành/thất bại quá thời hạn để dọn dẹp */
    @Query("SELECT j FROM VideoUploadJob j WHERE j.status IN (:statuses) AND j.updatedAt < :before")
    List<VideoUploadJob> findExpiredJobs(
            @Param("statuses") List<Status> statuses,
            @Param("before") LocalDateTime before);

    /** Xóa job cũ đã hoàn thành để tránh bảng phình */
    @Modifying
    @Query("DELETE FROM VideoUploadJob j WHERE j.status IN (:statuses) AND j.updatedAt < :before")
    int deleteExpiredJobs(
            @Param("statuses") List<Status> statuses,
            @Param("before") LocalDateTime before);
}
