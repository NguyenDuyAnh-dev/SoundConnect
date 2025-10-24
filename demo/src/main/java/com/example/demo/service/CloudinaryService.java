package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Upload image
    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", "images"
                ));
        return uploadResult.get("secure_url").toString();
    }

    // Upload audio/video (resumable)
    public String uploadAudio(MultipartFile file) throws IOException {
        // Giới hạn file 50MB trên free plan
        if (file.getSize() > 40_000_000L) {
            throw new RuntimeException("File quá lớn, tối đa 50MB trên free plan");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video", // audio cũng dùng "video"
                        "folder", "podcasts"
                ));
        return uploadResult.get("secure_url").toString();
    }
}

