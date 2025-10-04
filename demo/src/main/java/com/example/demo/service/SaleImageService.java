package com.example.demo.service;

import com.example.demo.dto.response.SaleImageResponse;
import com.example.demo.entity.SaleImage;
import com.example.demo.entity.SalePost;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.SalePostMapper;
import com.example.demo.repository.SaleImageRepository;
import com.example.demo.repository.SalePostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaleImageService {

    SaleImageRepository saleImageRepository;
    SalePostRepository salePostRepository;
    SalePostMapper salePostMapper;
    CloudinaryService cloudinaryService;

    /**  Upload ảnh mới cho bài đăng */
    public SaleImageResponse uploadImageToPost(Integer salePostId, MultipartFile file, boolean isPrimary) throws IOException {
        SalePost salePost = salePostRepository.findById(salePostId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Upload lên Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file);

        // Kiểm tra xem bài đăng đã có ảnh nào chưa
        List<SaleImage> existingImages = saleImageRepository.findBySalePost_Id(salePostId);
        boolean hasPrimary = existingImages.stream().anyMatch(SaleImage::getIsPrimary);

        // Nếu chưa có ảnh đại diện → tự động set ảnh đầu tiên là đại diện
        boolean finalPrimary = !hasPrimary || isPrimary;

        // Nếu có ảnh đại diện và isPrimary=true → reset ảnh cũ
        if (finalPrimary && hasPrimary) {
            existingImages.forEach(img -> img.setIsPrimary(false));
            saleImageRepository.saveAll(existingImages);
        }

        // Lưu ảnh mới
        SaleImage saleImage = new SaleImage();
        saleImage.setImageUrl(imageUrl);
        saleImage.setIsPrimary(finalPrimary);
        saleImage.setSalePost(salePost);
        saleImage.setCreatedAt(LocalDateTime.now());

        saleImageRepository.save(saleImage);

        return salePostMapper.toSaleImageResponse(saleImage);
    }

    /**  Lấy danh sách ảnh của một bài đăng */
    public List<SaleImageResponse> getImagesBySalePost(Integer salePostId) {
        salePostRepository.findById(salePostId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        List<SaleImage> images = saleImageRepository.findBySalePost_Id(salePostId);
        return salePostMapper.toSaleImageResponses(images);
    }

    /**  Đặt ảnh đại diện */
    public SaleImageResponse setPrimaryImage(Integer imageId) {
        SaleImage targetImage = saleImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_EXISTED));

        SalePost salePost = targetImage.getSalePost();
        if (salePost == null)
            throw new AppException(ErrorCode.POST_NOT_EXISTED);

        List<SaleImage> images = saleImageRepository.findBySalePost_Id(salePost.getId());
        for (SaleImage img : images) {
            img.setIsPrimary(img.getId().equals(imageId));
        }
        saleImageRepository.saveAll(images);

        return salePostMapper.toSaleImageResponse(targetImage);
    }

    /** Xóa ảnh */
    public boolean deleteSaleImage(Integer imageId) {
        SaleImage image = saleImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_EXISTED));

        SalePost salePost = image.getSalePost();
        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());

        saleImageRepository.delete(image);

        // Nếu ảnh bị xóa là đại diện → chọn ảnh khác làm đại diện
        if (wasPrimary && salePost != null) {
            List<SaleImage> remaining = saleImageRepository.findBySalePost_Id(salePost.getId());
            if (!remaining.isEmpty()) {
                SaleImage newPrimary = remaining.get(0);
                newPrimary.setIsPrimary(true);
                saleImageRepository.save(newPrimary);
            }
        }

        return true;
    }
}
