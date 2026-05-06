package com.li.bbs.interaction.repository;

import com.li.bbs.interaction.domain.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    boolean existsByUserIdAndTargetTypeAndTargetIdAndActionType(Long userId, String targetType, Long targetId, String actionType);
}

