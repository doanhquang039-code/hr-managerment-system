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

    /** Upload áº£nh/file thÃ´ng thÆ°á»ng, tráº£ vá» URL */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String resourceType = isVideo(file) ? "video" : "auto";
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", resourceType, "folder", folder));
        return result.get("secure_url").toString();
    }

    /** Upload áº£nh avatar â€” tráº£ vá» URL + public_id */
    public Map<?, ?> uploadAvatar(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", folder,
                        "transformation", "w_200,h_200,c_fill,g_face,q_auto,f_auto"
                ));
    }

    /** Upload document (PDF, image, Word) â€” tráº£ vá» full Map */
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

    /** Upload receipt/image nhá» */
    public Map<?, ?> uploadReceipt(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "auto",
                        "folder", folder,
                        "quality", "auto",
                        "fetch_format", "auto"
                ));
    }

    /** Upload video â€” tráº£ vá»  full Map Ä‘á»ƒ láº¥y public_id, duration, secure_url */
    public Map<?, ?> uploadVideo(MultipartFile file, String folder) throws IOException {
        // DÃ¹ng uploadLarge vá»›i InputStream Ä‘á»ƒ trÃ¡nh OutOfMemoryError vá»›i video lá»›n
        try (java.io.InputStream is = file.getInputStream()) {
            return cloudinary.uploader().uploadLarge(is,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", folder,
                            "chunk_size", 6_000_000   // 6MB per chunk
                    ));
        }
    }

    /**
     * Upload video từ InputStream (dùng cho async upload từ file tạm trên disk).
     * Hỗ trợ tên file gốc để Cloudinary nhận dạng format chính xác hơn.
     *
     * @param is           InputStream của file video
     * @param originalName Tên file gốc (để Cloudinary biết format)
     * @param folder       Thư mục Cloudinary
     */
    public Map<?, ?> uploadVideoFromStream(java.io.InputStream is, String originalName, String folder)
            throws IOException {
        return cloudinary.uploader().uploadLarge(is,
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "folder", folder,
                        "public_id", sanitizePublicId(originalName),
                        "chunk_size", 6_000_000,  // 6MB per chunk
                        "use_filename", true,
                        "unique_filename", true
                ));
    }

    /** Tạo public_id an toàn từ tên file gốc (bỏ ký tự đặc biệt) */
    private String sanitizePublicId(String filename) {
        if (filename == null) return "video_" + System.currentTimeMillis();
        // Bỏ extension, thay ký tự không hợp lệ bằng _
        String name = filename.replaceAll("\\.[^.]+$", ""); // bỏ extension
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase();
    }

    /** Upload áº£nh (avatar, thumbnail...) */
    public Map<?, ?> upload(MultipartFile file) throws IOException {
        String resourceType = isVideo(file) ? "video" : "auto";
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", resourceType));
    }

    /** XÃ³a video trÃªn Cloudinary theo public_id */
    public void deleteVideo(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId,
                ObjectUtils.asMap("resource_type", "video"));
    }

    /** XÃ³a áº£nh trÃªn Cloudinary theo public_id */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Táº¡o URL thumbnail tá»« video public_id.
     * Cloudinary tá»± generate frame Ä‘áº§u tiÃªn cá»§a video.
     */
    public String generateVideoThumbnail(String videoPublicId) {
        // Thay Ä‘á»•i extension thÃ nh .jpg Ä‘á»ƒ láº¥y thumbnail
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

