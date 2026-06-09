package com.li.bbs.interaction.repository;

import com.li.bbs.interaction.domain.Interaction;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface InteractionRepository {
    boolean existsByUserIdAndTargetTypeAndTargetIdAndActionType(Long userId, String targetType, Long targetId, String actionType);
    int insert(Interaction interaction);
}
