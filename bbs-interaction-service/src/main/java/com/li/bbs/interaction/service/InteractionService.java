package com.li.bbs.interaction.service;

import com.li.bbs.common.ApiResponse;
import com.li.bbs.interaction.client.NotificationClient;
import com.li.bbs.interaction.client.PostClient;
import com.li.bbs.interaction.domain.FollowRelation;
import com.li.bbs.interaction.domain.Interaction;
import com.li.bbs.interaction.repository.FollowRelationRepository;
import com.li.bbs.interaction.repository.InteractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final FollowRelationRepository followRelationRepository;
    private final PostClient postClient;
    private final NotificationClient notificationClient;

    public InteractionService(InteractionRepository interactionRepository,
                              FollowRelationRepository followRelationRepository,
                              PostClient postClient,
                              NotificationClient notificationClient) {
        this.interactionRepository = interactionRepository;
        this.followRelationRepository = followRelationRepository;
        this.postClient = postClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public void like(Long postId, Long userId) {
        savePostInteraction(postId, userId, "LIKE");
    }

    @Transactional
    public void favorite(Long postId, Long userId) {
        savePostInteraction(postId, userId, "FAVORITE");
    }

    @Transactional
    public void follow(Long targetUserId, Long userId) {
        if (!followRelationRepository.existsByUserIdAndTargetUserId(userId, targetUserId)) {
            FollowRelation relation = new FollowRelation();
            relation.setUserId(userId);
            relation.setTargetUserId(targetUserId);
            followRelationRepository.save(relation);
            notificationClient.sendInteractionNotification(Map.of(
                    "userId", targetUserId,
                    "type", "FOLLOW",
                    "payload", "fromUserId=" + userId
            ));
        }
    }

    public Set<Long> followers(Long targetUserId) {
        List<FollowRelation> list = followRelationRepository.findByTargetUserId(targetUserId);
        return list.stream().map(FollowRelation::getUserId).collect(Collectors.toSet());
    }

    private void savePostInteraction(Long postId, Long userId, String action) {
        ApiResponse<Map<String, Object>> metaResp = postClient.getMeta(postId);
        if (metaResp == null || metaResp.code() != 0 || metaResp.data() == null) {
            throw new IllegalArgumentException("post not found");
        }

        if (!interactionRepository.existsByUserIdAndTargetTypeAndTargetIdAndActionType(userId, "POST", postId, action)) {
            Interaction interaction = new Interaction();
            interaction.setUserId(userId);
            interaction.setTargetType("POST");
            interaction.setTargetId(postId);
            interaction.setActionType(action);
            interactionRepository.save(interaction);

            Long postAuthorId = Long.parseLong(String.valueOf(metaResp.data().get("authorId")));
            if (!postAuthorId.equals(userId)) {
                notificationClient.sendInteractionNotification(Map.of(
                        "userId", postAuthorId,
                        "type", action,
                        "payload", "postId=" + postId + ",fromUserId=" + userId
                ));
            }
        }
    }
}

