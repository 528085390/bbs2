package com.li.bbs.interaction.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.common.UserContext;
import com.li.bbs.interaction.service.InteractionService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ApiResponse<Void> like(@PathVariable("postId") Long postId,
                                  @RequestParam(value = "userId", required = false) Long userId,
                                  HttpServletRequest request) {
        Long uid = resolveUserId(userId, request);
        interactionService.like(postId, uid);
        return ApiResponse.ok();
    }

    @PostMapping("/posts/{postId}/favorite")
    public ApiResponse<Void> favorite(@PathVariable("postId") Long postId,
                                      @RequestParam(value = "userId", required = false) Long userId,
                                      HttpServletRequest request) {
        Long uid = resolveUserId(userId, request);
        interactionService.favorite(postId, uid);
        return ApiResponse.ok();
    }

    @PostMapping("/users/{targetUserId}/follow")
    public ApiResponse<Void> follow(@PathVariable("targetUserId") Long targetUserId,
                                    @RequestParam(value = "userId", required = false) Long userId,
                                    HttpServletRequest request) {
        Long uid = resolveUserId(userId, request);
        interactionService.follow(targetUserId, uid);
        return ApiResponse.ok();
    }

    @GetMapping("/users/{targetUserId}/followers")
    public ApiResponse<Set<Long>> getFollowers(@PathVariable("targetUserId") Long targetUserId) {
        return ApiResponse.ok(interactionService.followers(targetUserId));
    }

    /**
     * 获取当前登录用户的粉丝列表
     * 前端调用：GET /api/interactions/me/followers
     */
    @GetMapping("/me/followers")
    public ApiResponse<Set<Long>> getMyFollowers(HttpServletRequest request) {
        Long userId = UserContext.getUserId(request);
        if (userId == null) {
            return new ApiResponse<>(401, "请先登录", null);
        }
        return ApiResponse.ok(interactionService.followers(userId));
    }

    /**
     * 解析用户ID：优先使用请求参数，其次从请求头获取
     */
    private Long resolveUserId(Long paramUserId, HttpServletRequest request) {
        if (paramUserId != null) {
            return paramUserId;
        }
        Long headerUserId = UserContext.getUserId(request);
        if (headerUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空，请先登录");
        }
        return headerUserId;
    }
}
