package com.example.hr.service;

import com.example.hr.models.CollaborationGroup;
import com.example.hr.models.CollaborationGroupPost;
import com.example.hr.models.User;
import com.example.hr.repository.CollaborationGroupPostRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollaborationGroupPostService {

    private final CollaborationGroupPostRepository postRepository;
    private final GroupAccessService groupAccessService;
    private final AuthUserHelper authUserHelper;

    public CollaborationGroupPostService(CollaborationGroupPostRepository postRepository,
                                         GroupAccessService groupAccessService,
                                         AuthUserHelper authUserHelper) {
        this.postRepository = postRepository;
        this.groupAccessService = groupAccessService;
        this.authUserHelper = authUserHelper;
    }

    @Transactional(readOnly = true)
    public List<CollaborationGroupPost> getDefaultGroupFeed() {
        return postRepository.findFeedByGroup(groupAccessService.getDefaultGroup());
    }

    public CollaborationGroupPost createDefaultGroupUpdate(String content) {
        User author = getCurrentUser();
        if (author == null) {
            throw new IllegalStateException("Current user is required to create a group post.");
        }

        String normalizedContent = content == null ? "" : content.strip();
        if (normalizedContent.isBlank()) {
            throw new IllegalArgumentException("Group update content is required.");
        }

        CollaborationGroupPost post = new CollaborationGroupPost();
        post.setGroup(groupAccessService.getDefaultGroup());
        post.setAuthor(author);
        post.setContent(normalizedContent);
        post.setType("UPDATE");
        return postRepository.save(post);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authUserHelper.getCurrentUser(authentication);
    }
}
