package com.li.bbs.search.service;

import com.li.bbs.common.document.PostDocument;
import com.li.bbs.search.domain.SearchHistory;
import com.li.bbs.search.repository.SearchHistoryRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchHistoryRepository searchHistoryRepository;

    public SearchService(ElasticsearchOperations elasticsearchOperations,
                         SearchHistoryRepository searchHistoryRepository) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Transactional
    public List<Map<String, Object>> search(String q, Long userId) {
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(q);
        searchHistoryRepository.insert(history);

        String json = """
                {
                  "query": {
                    "multi_match": {
                      "query": "%s",
                      "fields": ["title^3", "content"]
                    }
                  }
                }
                """.formatted(escapeJson(q));

        SearchHits<PostDocument> hits = elasticsearchOperations.search(new StringQuery(json), PostDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> {
                    PostDocument doc = hit.getContent();
                    String snippet = doc.getContent();
                    if (snippet != null && snippet.length() > 80) {
                        snippet = snippet.substring(0, 80) + "...";
                    }
                    Map<String, Object> item = new HashMap<>();
                    item.put("postId", doc.getId());
                    item.put("title", doc.getTitle());
                    item.put("snippet", snippet);
                    return item;
                })
                .toList();
    }

    public List<String> suggest(String q) {
        List<String> fromHistory = searchHistoryRepository.suggestByPrefix(q);
        if (!fromHistory.isEmpty()) {
            return fromHistory;
        }

        String json = """
                {
                  "query": {
                    "match": {
                      "title": "%s"
                    }
                  },
                  "size": 10
                }
                """.formatted(escapeJson(q));

        SearchHits<PostDocument> hits = elasticsearchOperations.search(new StringQuery(json), PostDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .toList();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
