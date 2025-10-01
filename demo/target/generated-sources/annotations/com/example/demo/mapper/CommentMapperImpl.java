package com.example.demo.mapper;

import com.example.demo.dto.response.CommentDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-26T12:11:33+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDTO toCommentDTO(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDTO.CommentDTOBuilder commentDTO = CommentDTO.builder();

        commentDTO.avatar( commentAuthorAvatar( comment ) );
        commentDTO.authorName( commentAuthorName( comment ) );
        commentDTO.content( comment.getContent() );
        commentDTO.commentTime( comment.getCommentTime() );
        commentDTO.post( comment.getPost() );

        return commentDTO.build();
    }

    private String commentAuthorAvatar(Comment comment) {
        User author = comment.getAuthor();
        if ( author == null ) {
            return null;
        }
        return author.getAvatar();
    }

    private String commentAuthorName(Comment comment) {
        User author = comment.getAuthor();
        if ( author == null ) {
            return null;
        }
        return author.getName();
    }
}
