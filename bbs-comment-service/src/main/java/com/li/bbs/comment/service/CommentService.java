package com.li.bbs.comment.service;

import com.li.bbs.comment.client.NotificationClient;
import com.li.bbs.comment.client.PostClient;
import com.li.bbs.comment.domain.Comment;
import com.li.bbs.comment.dto.CommentRequest;
import com.li.bbs.comment.repository.CommentRepository;
import com.li.bbs.common.ApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostClient postClient;
    private final NotificationClient notificationClient;

    public CommentService(CommentRepository commentRepository, PostClient postClient, NotificationClient notificationClient) {
        this.commentRepository = commentRepository;
        this.postClient = postClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public Comment create(Long postId, CommentRequest request) {
        ApiResponse<Map<String, Object>> metaResp = postClient.getMeta(postId);
        if (metaResp == null || metaResp.code() != 0 || metaResp.data() == null) {
            throw new IllegalArgumentException("post not found");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(request.authorId());
        comment.setParentId(request.parentId());
        comment.setContent(request.content());

        if (request.parentId() != null) {
            Comment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("parent comment not found"));
            comment.setDepth(parent.getDepth() + 1);
        }

        Comment saved = commentRepository.save(comment);

        Long postAuthorId = Long.parseLong(String.valueOf(metaResp.data().get("authorId")));
        if (!postAuthorId.equals(request.authorId())) {
            notificationClient.sendCommentNotification(Map.of(
                    "userId", postAuthorId,
                    "type", "COMMENT",
                    "payload", "postId=" + postId + ",commentId=" + saved.getId() + ",fromUserId=" + request.authorId()
            ));
        }

        return saved;
    }

    public List<Comment> list(Long postId) {
        return commentRepository.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("comment not found"));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}

