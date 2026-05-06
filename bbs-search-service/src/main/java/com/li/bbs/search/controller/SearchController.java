package com.li.bbs.search.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.search.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> search(@RequestParam String q,
                                                          @RequestParam(required = false) Long userId) {
        return ApiResponse.ok(searchService.search(q, userId));
    }

    @GetMapping("/suggest")
    public ApiResponse<List<String>> suggest(@RequestParam String q) {
        return ApiResponse.ok(searchService.suggest(q));
    }
}
