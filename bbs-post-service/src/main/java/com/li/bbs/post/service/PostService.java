package com.li.bbs.post.service;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.post.client.SectionClient;
import com.li.bbs.post.domain.Post;
import com.li.bbs.post.dto.PostRequest;
import com.li.bbs.post.repository.PostRepository;
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
        return postRepository.save(post);
    }

    @Transactional
    public Post getAndIncreaseView(Long id) {
        Post post = get(id);
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long id, PostRequest request) {
        Post post = get(id);
        post.setSectionId(request.sectionId());
        post.setAuthorId(request.authorId());
        post.setTitle(request.title());
        post.setContent(request.content());
        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public void pin(Long id, boolean value) {
        Post post = get(id);
        post.setPinned(value);
        postRepository.save(post);
    }

    @Transactional
    public void feature(Long id, boolean value) {
        Post post = get(id);
        post.setFeatured(value);
        postRepository.save(post);
    }

    public boolean exists(Long id) {
        return postRepository.existsById(id);
    }

    public Post get(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("post not found"));
    }

    public List<Post> search(String q) {
        return postRepository.fullTextSearch(q + "*");
    }

    public List<String> suggest(String q) {
        return postRepository.findTop10ByTitleStartingWithIgnoreCaseOrderByCreatedAtDesc(q)
                .stream().map(Post::getTitle).distinct().toList();
    }
}

