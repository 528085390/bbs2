package com.li.bbs.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequest(
        @NotNull Long sectionId,
        @NotNull Long authorId,
        @NotBlank String title,
        @NotBlank String content
) {
}

