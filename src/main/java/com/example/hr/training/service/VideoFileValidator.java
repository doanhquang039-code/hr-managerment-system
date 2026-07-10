package com.example.hr.training.service;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Validator bảo mật cho file video upload.
 *
 * <p>Defense-in-depth gồm 3 lớp:</p>
 * <ol>
 *   <li>Kiểm tra Content-Type do browser gửi (lớp cơ bản)</li>
 *   <li>Kiểm tra extension của tên file</li>
 *   <li>Đọc magic bytes thực sự bằng Apache Tika (lớp quan trọng nhất —
 *       phát hiện file .exe đổi tên thành .mp4, v.v.)</li>
 * </ol>
 *
 * <p>Lưu ý: Không cần ClamAV ở đây vì Cloudinary đã có
 * moderation pipeline riêng. Tika đủ để chặn file giả mạo MIME type.</p>
 */
@Component
public class VideoFileValidator {

    private static final Logger log = LoggerFactory.getLogger(VideoFileValidator.class);

    /** MIME types được phép upload */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime",        // .mov
            "video/x-msvideo",        // .avi
            "video/x-matroska",       // .mkv
            "video/3gpp",             // .3gp
            "video/mpeg"              // .mpeg
    );

    /** Extension được phép (lowercase) */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".mp4", ".webm", ".mov", ".avi", ".mkv", ".3gp", ".mpeg", ".mpg"
    );

    /** Kích thước tối đa: 500 MB */
    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024;

    /**
     * Tika instance — thread-safe, dùng chung được.
     * Chỉ load magic bytes, không cần tika-parsers (nhanh).
     */
    private static final Tika TIKA = new Tika();

    // ----------------------------------------------------------------

    /**
     * Validate đầy đủ. Ném {@link VideoValidationException} nếu file không hợp lệ.
     *
     * @param file file nhận từ MultipartRequest
     * @throws VideoValidationException nếu file vi phạm bất kỳ quy tắc nào
     * @throws IOException              nếu không đọc được stream của file
     */
    public void validate(MultipartFile file) throws IOException, VideoValidationException {
        // 1. Kiểm tra rỗng
        if (file == null || file.isEmpty()) {
            throw new VideoValidationException("File không được để trống.");
        }

        // 2. Kiểm tra kích thước
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new VideoValidationException(
                    String.format("File quá lớn (%.1f MB). Tối đa 500 MB.",
                            file.getSize() / (1024.0 * 1024)));
        }

        // 3. Kiểm tra extension tên file
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new VideoValidationException("Tên file không hợp lệ.");
        }

        String lowerName = originalName.toLowerCase();
        boolean extOk = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!extOk) {
            throw new VideoValidationException(
                    "Extension không hợp lệ. Chỉ chấp nhận: " + ALLOWED_EXTENSIONS);
        }

        // 4. Kiểm tra Content-Type browser gửi (cảnh báo nếu sai, chưa chặn)
        String browserMime = file.getContentType();
        if (browserMime == null || !browserMime.startsWith("video/")) {
            log.warn("[VIDEO-SECURITY] Browser gửi Content-Type nghi ngờ: {} | File: {}",
                    browserMime, originalName);
            // Không chặn ngay — để Tika quyết định bên dưới
        }

        // 5. *** QUAN TRỌNG NHẤT: Đọc magic bytes bằng Tika ***
        //    Tika đọc vài trăm bytes đầu của file để xác định loại thực sự.
        //    Kẻ tấn công không thể qua được bước này chỉ bằng đổi tên hay sửa header HTTP.
        String detectedMime;
        try (InputStream is = file.getInputStream()) {
            detectedMime = TIKA.detect(is, originalName);
        }

        if (!ALLOWED_MIME_TYPES.contains(detectedMime)) {
            // Log cảnh báo bảo mật
            log.warn("[VIDEO-SECURITY] *** PHÁT HIỆN FILE GIẢ MẠO! *** " +
                            "Tên: {} | Browser MIME: {} | Tika MIME thực: {} | Size: {} bytes",
                    originalName, browserMime, detectedMime, file.getSize());

            throw new VideoValidationException(
                    String.format(
                            "File bị từ chối! Loại file thực sự là '%s', không phải video hợp lệ. " +
                            "Chỉ chấp nhận file video: mp4, webm, mov, avi, mkv.",
                            detectedMime));
        }

        // Log thành công
        log.info("[VIDEO-SECURITY] File hợp lệ: {} | MIME: {} | Size: {:.1f} MB",
                originalName, detectedMime, file.getSize() / (1024.0 * 1024));
    }

    // ----------------------------------------------------------------

    /**
     * Exception nội bộ cho video validation.
     * Runtime exception để không bắt buộc phải khai báo throws ở mọi nơi.
     */
    public static class VideoValidationException extends RuntimeException {
        public VideoValidationException(String message) {
            super(message);
        }
    }
}
