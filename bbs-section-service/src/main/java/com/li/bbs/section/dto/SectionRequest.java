package com.li.bbs.section.dto;

import jakarta.validation.constraints.NotBlank;

public record SectionRequest(
        @NotBlank String title,
        String description,
        Integer orderIndex,
        String visibility
) {
}

