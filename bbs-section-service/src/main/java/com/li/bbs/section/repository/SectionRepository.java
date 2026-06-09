package com.li.bbs.section.repository;

import com.li.bbs.section.domain.Section;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SectionRepository {
    List<Section> findAllByOrderByOrderIndexAscIdAsc();
    Section selectById(Long id);
    int insert(Section section);
    int update(Section section);
    int deleteById(Long id);
}
