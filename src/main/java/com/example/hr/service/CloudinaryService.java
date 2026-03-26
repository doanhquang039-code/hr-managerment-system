package com.example.hr.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        // Xác định loại resource (video hoặc image)
        String resourceType = file.getContentType().startsWith("video") ? "video" : "auto";
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "folder", folderName
                ));
        
        // Trả về URL của file sau khi upload thành công
        return uploadResult.get("secure_url").toString();
    }
}