package com.example.demo.mapper;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Post;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T22:27:11+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PostResponse toPostResponse(Post post) {
        if ( post == null ) {
            return null;
        }

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        postResponse.author( userMapper.toUserResponse( post.getAuthor() ) );
        postResponse.content( post.getContent() );
        postResponse.id( post.getId() );
        postResponse.location( post.getLocation() );
        postResponse.media( post.getMedia() );
        postResponse.postTime( post.getPostTime() );
        postResponse.postType( post.getPostType() );
        postResponse.status( post.getStatus() );
        postResponse.visibility( post.getVisibility() );

        postResponse.comment( post.getCommentList().size() );
        postResponse.reaction( post.getReactions().size() );

        return postResponse.build();
    }
}
