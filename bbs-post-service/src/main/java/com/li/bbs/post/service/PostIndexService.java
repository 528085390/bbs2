package com.li.bbs.post.service;

import com.li.bbs.common.document.PostDocument;
import com.li.bbs.post.domain.Post;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

@Service
public class PostIndexService {

    private static final Logger log = LoggerFactory.getLogger(PostIndexService.class);

    private final ElasticsearchOperations elasticsearchOperations;

    public PostIndexService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @PostConstruct
    public void initIndex() {
        try {
            IndexOperations ops = elasticsearchOperations.indexOps(PostDocument.class);
            if (!ops.exists()) {
                ops.create();
                ops.putMapping(ops.createMapping());
                log.info("ES index 'posts' created with IK analyzer mapping");
            }
        } catch (Exception e) {
            log.warn("ES not available yet, index 'posts' will be created on first write: {}", e.getMessage());
        }
    }

    public void indexPost(Post post) {
        elasticsearchOperations.save(toDocument(post));
    }

    public void deletePost(Long id) {
        elasticsearchOperations.delete(String.valueOf(id), PostDocument.class);
    }

    private PostDocument toDocument(Post post) {
        return new PostDocument(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                post.getSectionId(),
                post.getCreatedAt()
        );
    }
}
