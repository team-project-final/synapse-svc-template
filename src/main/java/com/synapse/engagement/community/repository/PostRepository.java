package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
