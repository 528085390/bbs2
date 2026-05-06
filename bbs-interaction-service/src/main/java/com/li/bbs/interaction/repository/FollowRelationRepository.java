package com.li.bbs.interaction.repository;

import com.li.bbs.interaction.domain.FollowRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRelationRepository extends JpaRepository<FollowRelation, Long> {
    boolean existsByUserIdAndTargetUserId(Long userId, Long targetUserId);
    List<FollowRelation> findByTargetUserId(Long targetUserId);
}

