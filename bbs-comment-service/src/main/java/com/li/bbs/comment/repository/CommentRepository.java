package com.li.bbs.comment.repository;

import com.li.bbs.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);
}

