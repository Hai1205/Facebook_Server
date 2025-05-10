package com.Server.utils.mapper;

import com.Server.dto.CommentDTO;
import com.Server.entity.Comment;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public static CommentDTO mapEntityToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setId(comment.getId());
        commentDTO.setText(comment.getText());
        commentDTO.setStatus(comment.getStatus().toString());
        commentDTO.setReportCount(comment.getReportCount());
        commentDTO.setCreatedAt(comment.getCreatedAt());
        commentDTO.setUpdatedAt(comment.getCreatedAt());

        return commentDTO;
    }

    public static List<CommentDTO> mapListEntityToListDTO(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public static CommentDTO mapEntityToDTOFull(Comment comment) {
        CommentDTO commentDTO = mapEntityToDTO(comment);

        if (comment.getUser() != null) {
            commentDTO.setUser(UserMapper.mapEntityToDTO(comment.getUser()));
        }

        return commentDTO;
    }

    public static List<CommentDTO> mapListEntityToListDTOFull(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::mapEntityToDTOFull)
                .collect(Collectors.toList());
    }
}
