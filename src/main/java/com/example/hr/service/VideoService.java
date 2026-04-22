package com.example.hr.service;

import com.example.hr.models.TrainingVideo;
import com.example.hr.models.User;
import com.example.hr.repository.TrainingVideoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class VideoService {

    private final TrainingVideoRepository videoRepository;
    private final CloudinaryService cloudinaryService;

    public VideoService(TrainingVideoRepository videoRepository,
                        CloudinaryService cloudinaryService) {
        this.videoRepository = videoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // ---- Query ----

    public List<TrainingVideo> findAll() {
        return videoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Cacheable(value = "videoLibrary", key = "'published'")
    public List<TrainingVideo> findPublished() {
        return videoRepository.findByIsPublishedTrueOrderByIdDesc();
    }

    public Optional<TrainingVideo> findById(Integer id) {
        return videoRepository.findById(id);
    }

    public List<TrainingVideo> searchVideos(String keyword, String category) {
        boolean hasKeyword  = keyword  != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasKeyword && hasCategory) {
            return videoRepository.findByTitleContainingIgnoreCaseAndCategory(keyword, category);
        }
        if (hasKeyword) {
            return videoRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                    keyword, keyword, Sort.by(Sort.Direction.DESC, "id"));
        }
        if (hasCategory) {
            return videoRepository.findByCategory(category);
        }
        return findPublished();
    }

    public List<String> findDistinctCategories() {
        return videoRepository.findDistinctCategories();
    }

    // ---- Upload to Cloudinary ----

    /**
     * Upload video lên Cloudinary, lưu metadata vào DB.
     */
    @CacheEvict(value = "videoLibrary", allEntries = true)
    public TrainingVideo uploadVideo(MultipartFile file,
                                     String title,
                                     String description,
                                     String category,
                                     String tags,
                                     User uploader) throws IOException {
        // Upload lên Cloudinary
        Map<?, ?> result = cloudinaryService.uploadVideo(file, "hr_training_videos");

        String videoUrl     = result.get("secure_url").toString();
        String publicId     = result.get("public_id").toString();
        String thumbnailUrl = cloudinaryService.generateVideoThumbnail(publicId);

        // Lấy duration nếu Cloudinary trả về
        Integer durationSec = 0;
        Object dur = result.get("duration");
        if (dur != null) {
            durationSec = (int) Math.round(Double.parseDouble(dur.toString()));
        }

        TrainingVideo video = new TrainingVideo();
        video.setTitle(title);
        video.setDescription(description);
        video.setCategory(category);
        video.setTags(tags);
        video.setVideoUrl(videoUrl);
        video.setPublicId(publicId);
        video.setThumbnailUrl(thumbnailUrl);
        video.setDurationSec(durationSec);
        video.setIsPublished(true);
        video.setViewCount(0);
        video.setUploader(uploader);

        return videoRepository.save(video);
    }

    // ---- Update metadata ----

    public TrainingVideo updateMetadata(Integer id, String title, String description,
                                         String category, String tags, Boolean isPublished) {
        TrainingVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại: " + id));
        if (title       != null) video.setTitle(title);
        if (description != null) video.setDescription(description);
        if (category    != null) video.setCategory(category);
        if (tags        != null) video.setTags(tags);
        if (isPublished != null) video.setIsPublished(isPublished);
        video.setUpdatedAt(LocalDateTime.now());
        return videoRepository.save(video);
    }

    // ---- Delete ----

    @CacheEvict(value = "videoLibrary", allEntries = true)
    public void deleteVideo(Integer id) throws IOException {
        TrainingVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại: " + id));

        // Xóa trên Cloudinary nếu có publicId
        if (video.getPublicId() != null && !video.getPublicId().isBlank()) {
            cloudinaryService.deleteVideo(video.getPublicId());
        }
        videoRepository.deleteById(id);
    }

    // ---- Increment view ----

    public void incrementView(Integer id) {
        videoRepository.findById(id).ifPresent(v -> {
            v.setViewCount((v.getViewCount() != null ? v.getViewCount() : 0) + 1);
            videoRepository.save(v);
        });
    }

    // ---- Toggle publish ----

    public TrainingVideo togglePublish(Integer id) {
        TrainingVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại: " + id));
        video.setIsPublished(!Boolean.TRUE.equals(video.getIsPublished()));
        video.setUpdatedAt(LocalDateTime.now());
        return videoRepository.save(video);
    }
}
