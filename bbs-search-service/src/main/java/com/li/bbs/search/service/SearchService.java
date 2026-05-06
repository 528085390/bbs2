package com.li.bbs.search.service;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.search.client.PostClient;
import com.li.bbs.search.domain.SearchHistory;
import com.li.bbs.search.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final PostClient postClient;
    private final SearchHistoryRepository searchHistoryRepository;

    public SearchService(PostClient postClient, SearchHistoryRepository searchHistoryRepository) {
        this.postClient = postClient;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Transactional
    public List<Map<String, Object>> search(String q, Long userId) {
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(q);
        searchHistoryRepository.save(history);

        ApiResponse<List<Map<String, Object>>> resp = postClient.search(q);
        if (resp == null || resp.code() != 0 || resp.data() == null) {
            return List.of();
        }

        return resp.data().stream().map(item -> {
            String content = String.valueOf(item.getOrDefault("content", ""));
            String snippet = highlight(content, q);
            return Map.of(
                    "postId", item.get("id"),
                    "title", item.get("title"),
                    "snippet", snippet
            );
        }).toList();
    }

    public List<String> suggest(String q) {
        List<String> fromHistory = searchHistoryRepository.suggestByPrefix(q);
        if (!fromHistory.isEmpty()) {
            return fromHistory;
        }

        ApiResponse<List<String>> resp = postClient.suggest(q);
        if (resp == null || resp.code() != 0 || resp.data() == null) {
            return List.of();
        }
        return resp.data();
    }

    private String highlight(String content, String q) {
        if (content == null || content.isBlank()) {
            return "";
        }
        int idx = content.toLowerCase().indexOf(q.toLowerCase());
        if (idx < 0) {
            return content.length() > 80 ? content.substring(0, 80) + "..." : content;
        }
        int start = Math.max(0, idx - 20);
        int end = Math.min(content.length(), idx + q.length() + 20);
        String snippet = content.substring(start, end);
        return snippet.replaceAll("(?i)" + java.util.regex.Pattern.quote(q), "<em>" + q + "</em>");
    }
}

