package com.synapse.engagement.community.infrastructure.persistence;

import com.synapse.engagement.community.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

interface CommentJpaRepository extends JpaRepository<Comment, Long> {
}
