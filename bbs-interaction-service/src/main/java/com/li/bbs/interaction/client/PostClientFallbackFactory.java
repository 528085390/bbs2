package com.li.bbs.interaction.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PostClientFallbackFactory implements FallbackFactory<PostClient> {

    @Override
    public PostClient create(Throwable cause) {
        return new PostClient() {
            @Override
            public ApiResponse<Map<String, Object>> getMeta(Long id) {
                return new ApiResponse<>(-1, "post service unavailable: " + cause.getMessage(), null);
            }
        };
    }
}
