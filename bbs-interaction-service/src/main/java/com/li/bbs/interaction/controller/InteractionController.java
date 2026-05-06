package com.li.bbs.interaction.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.interaction.service.InteractionService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/posts/{postId}/like")
    public ApiResponse<Void> like(@PathVariable Long postId, @RequestParam Long userId) {
        interactionService.like(postId, userId);
        return ApiResponse.ok();
    }

    @PostMapping("/posts/{postId}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long postId, @RequestParam Long userId) {
        interactionService.favorite(postId, userId);
        return ApiResponse.ok();
    }

    @PostMapping("/users/{targetUserId}/follow")
    public ApiResponse<Void> follow(@PathVariable Long targetUserId, @RequestParam Long userId) {
        interactionService.follow(targetUserId, userId);
        return ApiResponse.ok();
    }

    @GetMapping("/users/{targetUserId}/followers")
    public ApiResponse<Set<Long>> getFollowers(@PathVariable Long targetUserId) {
        return ApiResponse.ok(interactionService.followers(targetUserId));
    }
}
