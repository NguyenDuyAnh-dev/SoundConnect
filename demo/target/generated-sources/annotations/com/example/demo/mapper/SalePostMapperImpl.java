package com.example.demo.mapper;

import com.example.demo.dto.request.SalePostCreateRequest;
import com.example.demo.dto.request.SalePostUpdateRequest;
import com.example.demo.dto.response.SaleImageResponse;
import com.example.demo.dto.response.SalePostForCategoryResponse;
import com.example.demo.entity.SaleImage;
import com.example.demo.entity.SalePost;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-04T22:14:31+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class SalePostMapperImpl implements SalePostMapper {

    @Override
    public SalePostForCategoryResponse toSalePostForCategoryResponse(SalePost salePost) {
        if ( salePost == null ) {
            return null;
        }

        SalePostForCategoryResponse salePostForCategoryResponse = new SalePostForCategoryResponse();

        salePostForCategoryResponse.setId( salePost.getId() );
        salePostForCategoryResponse.setTitle( salePost.getTitle() );
        salePostForCategoryResponse.setDescription( salePost.getDescription() );
        salePostForCategoryResponse.setPrice( salePost.getPrice() );
        salePostForCategoryResponse.setCreatedAt( salePost.getCreatedAt() );

        return salePostForCategoryResponse;
    }

    @Override
    public List<SalePostForCategoryResponse> toSalePostForCategoryResponses(List<SalePost> salePosts) {
        if ( salePosts == null ) {
            return null;
        }

        List<SalePostForCategoryResponse> list = new ArrayList<SalePostForCategoryResponse>( salePosts.size() );
        for ( SalePost salePost : salePosts ) {
            list.add( toSalePostForCategoryResponse( salePost ) );
        }

        return list;
    }

    @Override
    public SalePost toSalePost(SalePostCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        SalePost.SalePostBuilder salePost = SalePost.builder();

        salePost.title( request.getTitle() );
        salePost.description( request.getDescription() );
        salePost.price( request.getPrice() );
        salePost.quantity( request.getQuantity() );
        salePost.location( request.getLocation() );
        salePost.commissionRate( request.getCommissionRate() );

        return salePost.build();
    }

    @Override
    public SaleImageResponse toSaleImageResponse(SaleImage saleImage) {
        if ( saleImage == null ) {
            return null;
        }

        SaleImageResponse saleImageResponse = new SaleImageResponse();

        saleImageResponse.setId( saleImage.getId() );
        saleImageResponse.setImageUrl( saleImage.getImageUrl() );
        saleImageResponse.setIsPrimary( saleImage.getIsPrimary() );

        return saleImageResponse;
    }

    @Override
    public List<SaleImageResponse> toSaleImageResponses(List<SaleImage> images) {
        if ( images == null ) {
            return null;
        }

        List<SaleImageResponse> list = new ArrayList<SaleImageResponse>( images.size() );
        for ( SaleImage saleImage : images ) {
            list.add( toSaleImageResponse( saleImage ) );
        }

        return list;
    }

    @Override
    public void updateSalePostFromRequest(SalePostUpdateRequest request, SalePost salePost) {
        if ( request == null ) {
            return;
        }

        salePost.setTitle( request.getTitle() );
        salePost.setDescription( request.getDescription() );
        salePost.setPrice( request.getPrice() );
        salePost.setQuantity( request.getQuantity() );
        salePost.setLocation( request.getLocation() );
        salePost.setCommissionRate( request.getCommissionRate() );
        salePost.setStatus( request.getStatus() );
    }
}
