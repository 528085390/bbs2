package com.li.bbs.interaction.repository;

import com.li.bbs.interaction.domain.FollowRelation;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FollowRelationRepository {
    boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId);
    List<FollowRelation> findByTargetUserId(Long targetUserId);
    int insert(FollowRelation relation);
}
