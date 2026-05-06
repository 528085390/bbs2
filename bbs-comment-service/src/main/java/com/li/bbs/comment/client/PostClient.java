package com.li.bbs.comment.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "bbs-post-service")
public interface PostClient {

    @GetMapping("/api/posts/{id}/meta")
    ApiResponse<Map<String, Object>> getMeta(@PathVariable("id") Long id);
}

