package com.li.bbs.file.controller;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.file.domain.FileMeta;
import com.li.bbs.file.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/avatar")
    public ApiResponse<FileMeta> uploadAvatar(@RequestPart("file") MultipartFile file,
                                              @RequestParam(required = false) Long ownerId) throws IOException {
        return ApiResponse.ok(fileService.upload("AVATAR", ownerId, file, "avatars"));
    }

    @PostMapping("/posts/{postId}/image")
    public ApiResponse<FileMeta> uploadPostImage(@PathVariable Long postId,
                                                 @RequestPart("file") MultipartFile file,
                                                 @RequestParam(required = false) Long ownerId) throws IOException {
        Long finalOwnerId = ownerId == null ? postId : ownerId;
        return ApiResponse.ok(fileService.upload("POST", finalOwnerId, file, "posts/" + postId));
    }

    @GetMapping("/{id}")
    public ApiResponse<FileMeta> get(@PathVariable Long id) {
        return ApiResponse.ok(fileService.get(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) throws IOException {
        fileService.delete(id);
        return ApiResponse.ok();
    }
}
