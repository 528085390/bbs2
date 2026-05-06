package com.li.bbs.search.repository;

import com.li.bbs.search.domain.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query(value = "SELECT keyword FROM search_history WHERE keyword LIKE CONCAT(?1, '%') GROUP BY keyword ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery = true)
    List<String> suggestByPrefix(String prefix);
}

