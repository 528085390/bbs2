package com.li.bbs.post.repository;

import com.li.bbs.post.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PostRepository {
    Post selectById(Long id);
    int insert(Post post);
    int update(Post post);
    int deleteById(Long id);
    int increaseViewCount(Long id);
    int updatePinned(@Param("id") Long id, @Param("value") boolean value);
    int updateFeatured(@Param("id") Long id, @Param("value") boolean value);
    boolean existsById(Long id);
    List<Post> findBySectionIdOrderByCreatedAtDesc(@Param("sectionId") Long sectionId);
}
