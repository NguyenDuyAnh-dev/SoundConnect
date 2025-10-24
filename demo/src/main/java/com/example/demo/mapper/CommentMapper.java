package com.example.demo.mapper;


import com.example.demo.dto.response.CommentDTO;
import com.example.demo.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "avatar", source = "author.avatar")
    @Mapping(target = "authorName", source = "author.name")
    CommentDTO toCommentDTO(Comment comment);


}

