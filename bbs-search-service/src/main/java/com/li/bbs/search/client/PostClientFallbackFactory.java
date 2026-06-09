package com.li.bbs.search.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PostClientFallbackFactory implements FallbackFactory<PostClient> {

    @Override
    public PostClient create(Throwable cause) {
        return new PostClient() {
            @Override
            public ApiResponse<List<Map<String, Object>>> search(String q) {
                return ApiResponse.ok(List.of());
            }

            @Override
            public ApiResponse<List<String>> suggest(String q) {
                return ApiResponse.ok(List.of());
            }
        };
    }
}
