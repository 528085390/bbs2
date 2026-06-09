package com.li.bbs.file.repository;

import com.li.bbs.file.domain.FileMeta;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface FileMetaRepository {
    FileMeta findById(Long id);
    int insert(FileMeta meta);
    int deleteById(Long id);
}
