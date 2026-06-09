package com.li.bbs.comment.controller;

import com.li.bbs.comment.domain.Comment;
import com.li.bbs.comment.dto.CommentRequest;
import com.li.bbs.comment.service.CommentService;
import com.li.bbs.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ApiResponse<Comment> create(@PathVariable("postId") Long postId, @Valid @RequestBody CommentRequest request) {
        try {
            return ApiResponse.ok(commentService.create(postId, request));
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        }
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ApiResponse<List<Comment>> list(@PathVariable("postId") Long postId) {
        return ApiResponse.ok(commentService.list(postId));
    }

    @DeleteMapping("/api/comments/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        commentService.delete(id);
        return ApiResponse.ok();
    }
}
