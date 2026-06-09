package com.li.bbs.post.client;

import com.li.bbs.common.ApiResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SectionClientFallbackFactory implements FallbackFactory<SectionClient> {

    @Override
    public SectionClient create(Throwable cause) {
        return id -> new ApiResponse<>(-1, "section service unavailable: " + cause.getMessage(), null);
    }
}
