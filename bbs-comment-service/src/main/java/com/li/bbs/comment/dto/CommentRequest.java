package com.li.bbs.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @NotNull Long authorId,
        Long parentId,
        @NotBlank String content
) {
}

