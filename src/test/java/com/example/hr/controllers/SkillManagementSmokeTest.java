package com.example.hr.controllers;

import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class SkillManagementSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminSkillListRendersTable() throws Exception {
        mockMvc.perform(get("/admin/skills"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/skill-list"))
                .andExpect(content().string(containsString("Danh Sách Kỹ Năng")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminSkillAddFormRenders() throws Exception {
        mockMvc.perform(get("/admin/skills/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/skill-form"))
                .andExpect(content().string(containsString("Lưu kỹ năng")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanCreateSkill() throws Exception {
        Optional<User> firstUser = userRepository.findAll().stream().findFirst();
        assumeTrue(firstUser.isPresent(), "Need at least one user to create a skill");

        String skillName = "Smoke Skill " + System.currentTimeMillis();
        mockMvc.perform(post("/admin/skills/save")
                        .with(csrf())
                        .param("userId", String.valueOf(firstUser.get().getId()))
                        .param("skillName", skillName)
                        .param("skillCategory", "TECHNICAL")
                        .param("skillLevel", "BEGINNER")
                        .param("yearsExp", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"));
    }
}
