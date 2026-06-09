package com.li.bbs.comment.repository;

import com.li.bbs.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CommentRepository {
    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);
    Comment selectById(Long id);
    int insert(Comment comment);
    int updateDeleted(Comment comment);
}
