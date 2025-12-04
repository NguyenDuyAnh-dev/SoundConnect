package com.example.demo.mapper;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Post;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-04T23:52:48+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Microsoft)"
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

        postResponse.id( post.getId() );
        postResponse.postTime( post.getPostTime() );
        postResponse.author( userMapper.toUserResponse( post.getAuthor() ) );
        postResponse.content( post.getContent() );
        postResponse.location( post.getLocation() );
        postResponse.media( post.getMedia() );
        postResponse.visibility( post.getVisibility() );
        postResponse.postType( post.getPostType() );
        postResponse.status( post.getStatus() );

        postResponse.comment( post.getCommentList().size() );
        postResponse.reaction( post.getReactions().size() );

        return postResponse.build();
    }
}
