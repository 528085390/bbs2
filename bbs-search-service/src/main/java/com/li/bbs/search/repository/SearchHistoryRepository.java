package com.li.bbs.search.repository;

import com.li.bbs.search.domain.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SearchHistoryRepository {
    int insert(SearchHistory history);
    List<String> suggestByPrefix(String prefix);
}
