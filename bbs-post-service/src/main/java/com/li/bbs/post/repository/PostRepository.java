package com.li.bbs.post.repository;

import com.li.bbs.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * FROM posts WHERE MATCH(title, content) AGAINST (?1 IN BOOLEAN MODE)", nativeQuery = true)
    List<Post> fullTextSearch(String keyword);

    List<Post> findTop10ByTitleStartingWithIgnoreCaseOrderByCreatedAtDesc(String keyword);
}

