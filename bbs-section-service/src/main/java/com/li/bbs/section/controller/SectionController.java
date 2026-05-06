package com.li.bbs.section.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.section.domain.Section;
import com.li.bbs.section.dto.SectionRequest;
import com.li.bbs.section.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public ApiResponse<List<Section>> list() {
        return ApiResponse.ok(sectionService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Section> get(@PathVariable Long id) {
        return ApiResponse.ok(sectionService.get(id));
    }

    @PostMapping
    public ApiResponse<Section> create(@Valid @RequestBody SectionRequest request) {
        return ApiResponse.ok(sectionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Section> update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return ApiResponse.ok(sectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ApiResponse.ok();
    }
}
