package com.li.bbs.file.repository;

import com.li.bbs.file.domain.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetaRepository extends JpaRepository<FileMeta, Long> {
}

