package com.li.bbs.section.service;

import com.li.bbs.section.domain.Section;
import com.li.bbs.section.dto.SectionRequest;
import com.li.bbs.section.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<Section> list() {
        return sectionRepository.findAllByOrderByOrderIndexAscIdAsc();
    }

    public Section get(Long id) {
        Section section = sectionRepository.selectById(id);
        if (section == null) {
            throw new IllegalArgumentException("section not found");
        }
        return section;
    }

    @Transactional
    public Section create(SectionRequest request) {
        Section section = new Section();
        apply(request, section);
        sectionRepository.insert(section);
        return section;
    }

    @Transactional
    public Section update(Long id, SectionRequest request) {
        Section section = get(id);
        apply(request, section);
        sectionRepository.update(section);
        return section;
    }

    @Transactional
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
