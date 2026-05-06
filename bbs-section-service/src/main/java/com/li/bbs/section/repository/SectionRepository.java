package com.li.bbs.section.repository;

import com.li.bbs.section.domain.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByOrderByOrderIndexAscIdAsc();
}

