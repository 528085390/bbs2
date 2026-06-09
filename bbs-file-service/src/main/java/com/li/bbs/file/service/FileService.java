package com.li.bbs.file.service;

import com.li.bbs.file.domain.FileMeta;
import com.li.bbs.file.repository.FileMetaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private final FileMetaRepository fileMetaRepository;

    @Value("${uploads.root:uploads}")
    private String uploadsRoot;

    public FileService(FileMetaRepository fileMetaRepository) {
        this.fileMetaRepository = fileMetaRepository;
    }

    public FileMeta upload(String ownerType, Long ownerId, MultipartFile file, String subFolder) throws IOException {
        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file.bin";
        String ext = "";
        int idx = originalName.lastIndexOf('.');
        if (idx >= 0) {
            ext = originalName.substring(idx);
        }

        Path folder = Paths.get(uploadsRoot, subFolder);
        Files.createDirectories(folder);
        Path target = folder.resolve(UUID.randomUUID() + ext);
        file.transferTo(target);

        FileMeta meta = new FileMeta();
        meta.setOwnerType(ownerType);
        meta.setOwnerId(ownerId);
        meta.setFilename(originalName);
        meta.setStoragePath(target.toString());
        meta.setContentType(file.getContentType());
        meta.setSize(file.getSize());
        fileMetaRepository.insert(meta);
        return meta;
    }

    public FileMeta get(Long id) {
        FileMeta meta = fileMetaRepository.findById(id);
        if (meta == null) {
            throw new IllegalArgumentException("file not found");
        }
        return meta;
    }

    public void delete(Long id) throws IOException {
        FileMeta meta = get(id);
        Files.deleteIfExists(Paths.get(meta.getStoragePath()));
        fileMetaRepository.deleteById(id);
    }
}
