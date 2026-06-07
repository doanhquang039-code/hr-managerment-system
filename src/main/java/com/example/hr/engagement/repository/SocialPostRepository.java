package com.example.hr.engagement.repository;

import com.example.hr.engagement.entity.SocialPost;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Integer> {
    
    List<SocialPost> findByUser(User user);
    
    List<SocialPost> findByType(String type);
    
    @Query("SELECT p FROM SocialPost p WHERE p.isPublic = true ORDER BY p.createdAt DESC")
    List<SocialPost> findPublicPostsOrderByCreatedAtDesc();
    
    @Query("SELECT p FROM SocialPost p ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<SocialPost> findTrendingPosts();
}


