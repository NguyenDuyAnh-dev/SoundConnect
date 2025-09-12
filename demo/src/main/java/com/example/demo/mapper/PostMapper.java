package com.example.demo.mapper;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CommentMapper.class})
public interface PostMapper {
    @Mapping(target = "comment", expression = "java(post.getCommentList().size())")
    @Mapping(target = "reaction", expression = "java(post.getReactions().size())")
    PostResponse toPostResponse(Post post);
}
