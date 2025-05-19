package com.Server.utils.mapper;

import com.Server.dto.PostDTO;
import com.Server.entity.Post;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PostMapper {
        public static PostDTO mapEntityToDTOFull(Post post) {
                PostDTO postDTO = mapEntityToDTO(post);

                if (post.getUser() != null) {
                        postDTO.setUser(UserMapper.mapEntityToDTO(post.getUser()));
                } else {
                        postDTO.setUser(null);
                }

                postDTO.setLikes(UserMapper.mapListEntityToListDTO(
                                post.getLikes().stream()
                                                .filter(Objects::nonNull)
                                                .toList()));
                postDTO.setShare(UserMapper.mapListEntityToListDTO(
                                post.getShares().stream()
                                                .filter(Objects::nonNull)
                                                .toList()));
                postDTO.setComments(CommentMapper.mapListEntityToListDTOFull(
                                post.getComments().stream()
                                                .filter(Objects::nonNull)
                                                .toList()));

                return postDTO;
        }

        public static List<PostDTO> mapListEntityToListDTOFull(List<Post> posts) {
                return posts.stream()
                                .map(PostMapper::mapEntityToDTOFull)
                                .collect(Collectors.toList());
        }

        public static List<PostDTO> mapListEntityToListDTO(List<Post> posts) {
                return posts.stream()
                                .map(PostMapper::mapEntityToDTO)
                                .collect(Collectors.toList());
        }

        public static PostDTO mapEntityToDTO(Post post) {
                PostDTO postDTO = new PostDTO();

                postDTO.setId(post.getId());
                postDTO.setContent(post.getContent());

                if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
                        postDTO.setMediaUrls(post.getMediaUrls());
                        postDTO.setMediaTypes(post.getMediaTypes());
                }

                postDTO.setPrivacy(post.getPrivacy());
                postDTO.setStatus(post.getStatus());
                postDTO.setReportCount(post.getReportCount());
                postDTO.setLikeCount(post.getLikes().size());
                postDTO.setCommentCount(post.getComments().size());
                postDTO.setShareCount(post.getShares().size());
                postDTO.setCreatedAt(post.getCreatedAt());
                postDTO.setUpdatedAt(post.getUpdatedAt());

                return postDTO;
        }
}
