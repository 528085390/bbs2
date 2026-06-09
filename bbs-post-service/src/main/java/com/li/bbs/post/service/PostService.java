package com.li.bbs.post.service;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.post.client.SectionClient;
import com.li.bbs.post.domain.Post;
import com.li.bbs.post.dto.PostRequest;
import com.li.bbs.post.repository.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SectionClient sectionClient;

    public PostService(PostRepository postRepository, SectionClient sectionClient) {
        this.postRepository = postRepository;
        this.sectionClient = sectionClient;
    }

    @Transactional
    @CacheEvict(value = "bbs:posts:section", key = "#request.sectionId()")
    public Post create(PostRequest request) {
        ApiResponse<?> sectionResp = sectionClient.getSection(request.sectionId());
        if (sectionResp == null || sectionResp.code() != 0 || sectionResp.data() == null) {
            throw new IllegalArgumentException("section not found");
        }

        Post post = new Post();
        post.setSectionId(request.sectionId());
        post.setAuthorId(request.authorId());
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setPinned(false);
        post.setFeatured(false);
        post.setViewCount(0L);
        postRepository.insert(post);
        return post;
    }

    @Transactional
    public Post getAndIncreaseView(Long id) {
        Post post = postRepository.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("post not found");
        }
        postRepository.increaseViewCount(id);
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bbs:posts", key = "#id"),
        @CacheEvict(value = "bbs:posts:section", allEntries = true)
    })
    public Post update(Long id, PostRequest request) {
        Post post = postRepository.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("post not found");
        }
        post.setSectionId(request.sectionId());
        post.setAuthorId(request.authorId());
        post.setTitle(request.title());
        post.setContent(request.content());
        postRepository.update(post);
        return post;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bbs:posts", key = "#id"),
        @CacheEvict(value = "bbs:posts:section", allEntries = true)
    })
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bbs:posts", key = "#id"),
        @CacheEvict(value = "bbs:posts:section", allEntries = true)
    })
    public void pin(Long id, boolean value) {
        postRepository.updatePinned(id, value);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "bbs:posts", key = "#id"),
        @CacheEvict(value = "bbs:posts:section", allEntries = true)
    })
    public void feature(Long id, boolean value) {
        postRepository.updateFeatured(id, value);
    }

    public boolean exists(Long id) {
        return postRepository.existsById(id);
    }

    @Cacheable(value = "bbs:posts", key = "#id")
    public Post get(Long id) {
        Post post = postRepository.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("post not found");
        }
        return post;
    }

    public List<Post> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return postRepository.findAllOrderByCreatedAtDesc();
        }
        String escaped = q.replace("+", " ")
                          .replace("-", " ")
                          .replace(">", " ")
                          .replace("<", " ")
                          .replace("(", " ")
                          .replace(")", " ")
                          .replace("~", " ")
                          .replace("*", " ")
                          .replace("\"", " ");
        return postRepository.fullTextSearch(escaped.trim() + "*");
    }

    @Cacheable(value = "bbs:posts:section", key = "#sectionId")
    public List<Post> listBySection(Long sectionId) {
        return postRepository.findBySectionIdOrderByCreatedAtDesc(sectionId);
    }

    public List<String> suggest(String q) {
        return postRepository.findTop10ByTitleStartingWithIgnoreCaseOrderByCreatedAtDesc(q)
                .stream().map(Post::getTitle).distinct().toList();
    }
}
