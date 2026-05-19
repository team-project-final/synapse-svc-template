package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
