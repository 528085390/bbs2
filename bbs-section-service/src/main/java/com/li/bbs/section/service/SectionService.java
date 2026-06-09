package com.li.bbs.section.service;

import com.li.bbs.section.domain.Section;
import com.li.bbs.section.dto.SectionRequest;
import com.li.bbs.section.repository.SectionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Cacheable(value = "bbs:sections", key = "'list'")
    public List<Section> list() {
        return sectionRepository.findAllByOrderByOrderIndexAscIdAsc();
    }

    @Cacheable(value = "bbs:sections", key = "#id")
    public Section get(Long id) {
        Section section = sectionRepository.selectById(id);
        if (section == null) {
            throw new IllegalArgumentException("section not found");
        }
        return section;
    }

    @Transactional
    @CacheEvict(value = "bbs:sections", allEntries = true)
    public Section create(SectionRequest request) {
        Section section = new Section();
        apply(request, section);
        sectionRepository.insert(section);
        return section;
    }

    @Transactional
    @CacheEvict(value = "bbs:sections", allEntries = true)
    public Section update(Long id, SectionRequest request) {
        Section section = sectionRepository.selectById(id);
        if (section == null) {
            throw new IllegalArgumentException("section not found");
        }
        apply(request, section);
        sectionRepository.update(section);
        return section;
    }

    @Transactional
    @CacheEvict(value = "bbs:sections", allEntries = true)
    public void delete(Long id) {
        sectionRepository.deleteById(id);
    }

    private void apply(SectionRequest request, Section section) {
        section.setTitle(request.title());
        section.setDescription(request.description());
        section.setOrderIndex(request.orderIndex() == null ? 0 : request.orderIndex());
        section.setVisibility(request.visibility() == null ? "PUBLIC" : request.visibility());
    }
}
