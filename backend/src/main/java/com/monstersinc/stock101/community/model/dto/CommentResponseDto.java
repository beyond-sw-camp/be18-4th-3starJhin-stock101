package com.monstersinc.stock101.community.model.dto;

import com.monstersinc.stock101.community.model.vo.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private long commentId;
    private String content;
    private String createdAt;
    private boolean isDeleted;
    private long postId;
    private long userId;
    private Long parentCommentId;

    private String userName;
    private String authorTierCode;
    private int totalCommentCount;
    private String imageUrl;

    public static CommentResponseDto of(Comment c) {
        return CommentResponseDto.builder()
                .commentId(c.getCommentId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .isDeleted(c.isDeleted())
                .postId(c.getPostId())
                .userId(c.getUserId())
                .parentCommentId(c.getParentCommentId())
                .userName(c.getUserName())
                .authorTierCode(c.getAuthorTierCode())
                .totalCommentCount(c.getTotalCommentCount())
                .imageUrl(c.getImageUrl())
                .build();
    }

    public static List<CommentResponseDto> of(List<Comment> comments) {
        return comments.stream().map(CommentResponseDto::of).toList();
    }
}
