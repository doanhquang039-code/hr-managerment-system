package com.example.hr.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /** Upload ảnh/file thông thường, trả về URL */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String resourceType = isVideo(file) ? "video" : "auto";
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", resourceType, "folder", folder));
        return result.get("secure_url").toString();
    }

    /** Upload ảnh avatar — trả về URL + public_id */
    public Map<?, ?> uploadAvatar(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", folder,
                        "transformation", "w_200,h_200,c_fill,g_face,q_auto,f_auto"
                ));
    }

    /** Upload document (PDF, image, Word) — trả về full Map */
    public Map<?, ?> uploadDocument(MultipartFile file, String folder) throws IOException {
        String ct = file.getContentType() != null ? file.getContentType() : "";
        String resourceType = ct.startsWith("image/") ? "image" : "raw";
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", resourceType,
                        "folder", folder,
                        "use_filename", true,
                        "unique_filename", true
                ));
    }

    /** Upload receipt/image nhỏ */
    public Map<?, ?> uploadReceipt(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "auto",
                        "folder", folder,
                        "quality", "auto",
                        "fetch_format", "auto"
                ));
    }

    /** Upload video — trả về full Map để lấy public_id, duration, secure_url */
    public Map<?, ?> uploadVideo(MultipartFile file, String folder) throws IOException {
        // Dùng uploadLarge với InputStream để tránh OutOfMemoryError với video lớn
        try (java.io.InputStream is = file.getInputStream()) {
            return cloudinary.uploader().uploadLarge(is,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", folder,
                            "chunk_size", 6_000_000   // 6MB per chunk
                    ));
        }
    }

    /** Upload ảnh (avatar, thumbnail...) */
    public Map<?, ?> upload(MultipartFile file) throws IOException {
        String resourceType = isVideo(file) ? "video" : "auto";
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", resourceType));
    }

    /** Xóa video trên Cloudinary theo public_id */
    public void deleteVideo(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId,
                ObjectUtils.asMap("resource_type", "video"));
    }

    /** Xóa ảnh trên Cloudinary theo public_id */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Tạo URL thumbnail từ video public_id.
     * Cloudinary tự generate frame đầu tiên của video.
     */
    public String generateVideoThumbnail(String videoPublicId) {
        // Thay đổi extension thành .jpg để lấy thumbnail
        return cloudinary.url()
                .resourceType("video")
                .format("jpg")
                .transformation(new com.cloudinary.Transformation()
                        .width(640).height(360).crop("fill").quality("auto"))
                .generate(videoPublicId);
    }

    private boolean isVideo(MultipartFile file) {
        return file.getContentType() != null && file.getContentType().startsWith("video");
    }
}