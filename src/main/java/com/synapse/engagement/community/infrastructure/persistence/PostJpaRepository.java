package com.synapse.engagement.community.infrastructure.persistence;

import com.synapse.engagement.community.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostJpaRepository extends JpaRepository<Post, Long> {
}
