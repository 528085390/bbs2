package com.li.bbs.search.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "bbs-post-service", fallbackFactory = PostClientFallbackFactory.class)
public interface PostClient {

    @GetMapping("/api/posts/internal/search")
    ApiResponse<List<Map<String, Object>>> search(@RequestParam("q") String q);

    @GetMapping("/api/posts/internal/suggest")
    ApiResponse<List<String>> suggest(@RequestParam("q") String q);
}

