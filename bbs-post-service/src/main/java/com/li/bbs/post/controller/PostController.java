package com.li.bbs.post.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.post.domain.Post;
import com.li.bbs.post.dto.PostRequest;
import com.li.bbs.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ApiResponse<Post> create(@Valid @RequestBody PostRequest request) {
        return ApiResponse.ok(postService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Post> get(@PathVariable Long id) {
        return ApiResponse.ok(postService.getAndIncreaseView(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Post> update(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return ApiResponse.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/pin")
    public ApiResponse<Void> pin(@PathVariable Long id, @RequestParam boolean value) {
        postService.pin(id, value);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/feature")
    public ApiResponse<Void> feature(@PathVariable Long id, @RequestParam boolean value) {
        postService.feature(id, value);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/exists")
    public ApiResponse<Boolean> exists(@PathVariable Long id) {
        return ApiResponse.ok(postService.exists(id));
    }

    @GetMapping("/{id}/meta")
    public ApiResponse<Map<String, Object>> meta(@PathVariable Long id) {
        Post post = postService.get(id);
        return ApiResponse.ok(Map.of(
                "id", post.getId(),
                "authorId", post.getAuthorId(),
                "sectionId", post.getSectionId(),
                "title", post.getTitle()
        ));
    }

    @GetMapping("/internal/search")
    public ApiResponse<List<Map<String, Object>>> internalSearch(@RequestParam String q) {
        List<Map<String, Object>> result = postService.search(q).stream()
                .map(post -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", post.getId());
                    item.put("title", post.getTitle());
                    item.put("content", post.getContent());
                    return item;
                })
                .toList();
        return ApiResponse.ok(result);
    }

    @GetMapping("/internal/suggest")
    public ApiResponse<List<String>> internalSuggest(@RequestParam String q) {
        return ApiResponse.ok(postService.suggest(q));
    }
}
