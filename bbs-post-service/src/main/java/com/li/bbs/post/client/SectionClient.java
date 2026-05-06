package com.li.bbs.post.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "bbs-section-service")
public interface SectionClient {

    @GetMapping("/api/sections/{id}")
    ApiResponse<Map<String, Object>> getSection(@PathVariable("id") Long id);
}

