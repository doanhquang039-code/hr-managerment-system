package com.example.hr.training.service;

import com.example.hr.service.CloudinaryService;
import com.example.hr.training.dto.VideoUploadStatusDto;
import com.example.hr.training.entity.TrainingVideo;
import com.example.hr.training.entity.VideoUploadJob;
import com.example.hr.training.entity.VideoUploadJob.Status;
import com.example.hr.models.User;
import com.example.hr.training.repository.TrainingVideoRepository;
import com.example.hr.training.repository.VideoUploadJobRepository;
import com.example.hr.training.service.VideoFileValidator.VideoValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    private final TrainingVideoRepository videoRepository;
    private final CloudinaryService cloudinaryService;
    private final VideoUploadJobRepository jobRepository;
    private final VideoFileValidator fileValidator;

    @Value("${video.upload.temp-dir:#{systemProperties['java.io.tmpdir']}/hr-video-uploads}")
    private String tempDirStr;

    @Value("${video.upload.job-expire-hours:24}")
    private int jobExpireHours;

    public VideoService(TrainingVideoRepository videoRepository,
                        CloudinaryService cloudinaryService,
                        VideoUploadJobRepository jobRepository,
                        VideoFileValidator fileValidator) {
        this.videoRepository = videoRepository;
        this.cloudinaryService = cloudinaryService;
        this.jobRepository = jobRepository;
        this.fileValidator = fileValidator;
    }

    // ================================================================
    // === QUERY (không đổi) ==========================================
    // ================================================================

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

    // ================================================================
    // === BƯỚC 1: Nhận file → validate ngay → lưu tạm → trả jobId  ==
    // ================================================================

    /**
     * Nhận MultipartFile từ controller, thực hiện VALIDATE NGAY (Tika magic bytes),
     * sau đó lưu file xuống thư mục tạm và tạo một {@link VideoUploadJob}.
     *
     * <p>Phương thức này trả về NGAY LẬP TỨC (< 1 giây) — không block HTTP thread.</p>
     *
     * @return jobId (UUID string) để client dùng polling
     * @throws VideoValidationException nếu file không hợp lệ (phát hiện ngay lập tức)
     * @throws IOException              nếu không lưu được file tạm
     */
    public String submitUploadAsync(MultipartFile file, String title, String description,
                                     String category, String tags, User uploader)
            throws IOException, VideoValidationException {

        // --- Bước A: Validate file (magic bytes) — chặn file độc hại NGAY ---
        fileValidator.validate(file);

        // --- Bước B: Lưu file tạm xuống disk ---
        Path tempDir = Paths.get(tempDirStr);
        Files.createDirectories(tempDir);

        String jobId = UUID.randomUUID().toString();
        String ext   = getExtension(file.getOriginalFilename());
        Path   tempFile = tempDir.resolve(jobId + ext);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("[UPLOAD-JOB] Lưu file tạm thành công: {} ({} bytes)", tempFile, Files.size(tempFile));

        // --- Bước C: Tạo job record trong DB ---
        VideoUploadJob job = new VideoUploadJob();
        job.setId(jobId);
        job.setStatus(Status.PENDING);
        job.setTempFilePath(tempFile.toString());
        job.setOriginalFilename(file.getOriginalFilename());
        job.setTitle(title);
        job.setDescription(description);
        job.setCategory(category);
        job.setTags(tags);
        job.setUploaderId(uploader != null ? uploader.getId() : null);
        job.setProgress(5);
        jobRepository.save(job);

        log.info("[UPLOAD-JOB] Đã tạo job {} cho user {}", jobId,
                uploader != null ? uploader.getUsername() : "unknown");
        return jobId;
    }

    // ================================================================
    // === BƯỚC 2: @Async — chạy upload Cloudinary trong thread pool  ==
    // ================================================================

    /**
     * Được gọi bởi controller SAU KHI trả response jobId về client.
     * Chạy trong thread pool "videoUploadExecutor" — không block HTTP thread.
     *
     * <p>Các bước trong background:</p>
     * <ol>
     *   <li>Cập nhật status → PROCESSING</li>
     *   <li>Đọc file tạm → upload lên Cloudinary (uploadLarge, chunked)</li>
     *   <li>Lưu TrainingVideo vào DB</li>
     *   <li>Cập nhật job status → DONE</li>
     *   <li>Xoá file tạm</li>
     * </ol>
     * Nếu có lỗi → status → FAILED, lưu error message.
     */
    @Async("videoUploadExecutor")
    public void processUploadJobAsync(String jobId) {
        VideoUploadJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("[UPLOAD-JOB] Không tìm thấy job: {}", jobId);
            return;
        }

        Path tempFile = Paths.get(job.getTempFilePath());

        try {
            // --- Cập nhật PROCESSING ---
            job.setStatus(Status.PROCESSING);
            job.setProgress(10);
            jobRepository.save(job);
            log.info("[UPLOAD-JOB] Bắt đầu xử lý job {} | File: {}", jobId, tempFile);

            // --- Upload lên Cloudinary ---
            job.setProgress(20);
            jobRepository.save(job);

            Map<?, ?> result;
            try (InputStream is = Files.newInputStream(tempFile)) {
                result = cloudinaryService.uploadVideoFromStream(is, job.getOriginalFilename(), "hr_training_videos");
            }

            job.setProgress(85);
            jobRepository.save(job);

            // --- Lấy thông tin từ Cloudinary response ---
            String videoUrl     = result.get("secure_url").toString();
            String publicId     = result.get("public_id").toString();
            String thumbnailUrl = cloudinaryService.generateVideoThumbnail(publicId);

            Integer durationSec = 0;
            Object dur = result.get("duration");
            if (dur != null) {
                durationSec = (int) Math.round(Double.parseDouble(dur.toString()));
            }

            // --- Lưu TrainingVideo ---
            TrainingVideo video = new TrainingVideo();
            video.setTitle(job.getTitle());
            video.setDescription(job.getDescription());
            video.setCategory(job.getCategory());
            video.setTags(job.getTags());
            video.setVideoUrl(videoUrl);
            video.setPublicId(publicId);
            video.setThumbnailUrl(thumbnailUrl);
            video.setDurationSec(durationSec);
            video.setIsPublished(true);
            video.setViewCount(0);
            // Không set uploader entity để tránh lazy load trong async thread
            // (uploader_id đã được lưu trong job)
            TrainingVideo saved = videoRepository.save(video);
            evictVideoLibraryCache();

            // --- Cập nhật DONE ---
            job.setStatus(Status.DONE);
            job.setProgress(100);
            job.setVideoId(saved.getId());
            jobRepository.save(job);
            log.info("[UPLOAD-JOB] ✅ Hoàn thành job {} → VideoId={}", jobId, saved.getId());

        } catch (Exception e) {
            log.error("[UPLOAD-JOB] ❌ Lỗi job {}: {}", jobId, e.getMessage(), e);
            job.setStatus(Status.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setProgress(0);
            jobRepository.save(job);
        } finally {
            // --- Xoá file tạm dù thành công hay thất bại ---
            try {
                Files.deleteIfExists(tempFile);
                log.debug("[UPLOAD-JOB] Đã xóa file tạm: {}", tempFile);
            } catch (IOException ex) {
                log.warn("[UPLOAD-JOB] Không xóa được file tạm {}: {}", tempFile, ex.getMessage());
            }
        }
    }

    // ================================================================
    // === BƯỚC 3: Client polling — lấy trạng thái job              ===
    // ================================================================

    /**
     * Trả về DTO trạng thái của job để client hiển thị progress bar.
     */
    public VideoUploadStatusDto getJobStatus(String jobId) {
        return jobRepository.findById(jobId)
                .map(job -> switch (job.getStatus()) {
                    case PENDING    -> VideoUploadStatusDto.pending(jobId);
                    case PROCESSING -> VideoUploadStatusDto.processing(jobId, job.getProgress(),
                            "Đang upload lên Cloudinary (" + job.getProgress() + "%)...");
                    case DONE       -> VideoUploadStatusDto.done(jobId, job.getVideoId());
                    case FAILED     -> VideoUploadStatusDto.failed(jobId, job.getErrorMessage());
                })
                .orElse(VideoUploadStatusDto.failed(jobId, "Job không tồn tại."));
    }

    // ================================================================
    // === Upload cũ (giữ lại để backward-compatible) =================
    // ================================================================

    /**
     * Upload đồng bộ — CHỈ dùng cho file nhỏ hoặc test.
     * Với video lớn, dùng submitUploadAsync() + processUploadJobAsync().
     *
     * @deprecated Dùng submitUploadAsync() thay thế.
     */
    @Deprecated
    @CacheEvict(value = "videoLibrary", allEntries = true)
    public TrainingVideo uploadVideo(MultipartFile file, String title, String description,
                                     String category, String tags, User uploader) throws IOException {
        // Validate trước
        fileValidator.validate(file);

        Map<?, ?> result = cloudinaryService.uploadVideo(file, "hr_training_videos");
        String videoUrl     = result.get("secure_url").toString();
        String publicId     = result.get("public_id").toString();
        String thumbnailUrl = cloudinaryService.generateVideoThumbnail(publicId);

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

    // ================================================================
    // === Metadata, Delete, Publish, View (không đổi) ================
    // ================================================================

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

    @CacheEvict(value = "videoLibrary", allEntries = true)
    public void deleteVideo(Integer id) throws IOException {
        TrainingVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại: " + id));
        if (video.getPublicId() != null && !video.getPublicId().isBlank()) {
            cloudinaryService.deleteVideo(video.getPublicId());
        }
        videoRepository.deleteById(id);
    }

    public void incrementView(Integer id) {
        videoRepository.findById(id).ifPresent(v -> {
            v.setViewCount((v.getViewCount() != null ? v.getViewCount() : 0) + 1);
            videoRepository.save(v);
        });
    }

    public TrainingVideo togglePublish(Integer id) {
        TrainingVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại: " + id));
        video.setIsPublished(!Boolean.TRUE.equals(video.getIsPublished()));
        video.setUpdatedAt(LocalDateTime.now());
        return videoRepository.save(video);
    }

    // ================================================================
    // === Scheduled: dọn dẹp job cũ mỗi ngày lúc 3 giờ sáng       ===
    // ================================================================

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(jobExpireHours);
        int deleted = jobRepository.deleteExpiredJobs(
                List.of(Status.DONE, Status.FAILED), cutoff);
        if (deleted > 0) {
            log.info("[UPLOAD-JOB] Đã dọn {} job cũ (trước {})", deleted, cutoff);
        }
    }

    // ================================================================
    // === Helper =====================================================
    // ================================================================

    @CacheEvict(value = "videoLibrary", allEntries = true)
    public void evictVideoLibraryCache() {
        // Được gọi từ async thread sau khi lưu video thành công
    }

    private String getExtension(String filename) {
        if (filename == null) return ".tmp";
        int idx = filename.lastIndexOf('.');
        return (idx >= 0) ? filename.substring(idx) : ".tmp";
    }
}
