package com.example.hr.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class GroupWorkspaceSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminGroupManagementRendersPermissions() throws Exception {
        mockMvc.perform(get("/admin/groups"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/group-management"))
                .andExpect(content().string(containsString("QUYỀN ĐANG BẬT")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void groupDashboardRendersWorkspace() throws Exception {
        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/dashboard"))
                .andExpect(content().string(containsString("Group Workspace")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void groupMembersRendersEffectiveMembers() throws Exception {
        mockMvc.perform(get("/groups/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/members"))
                .andExpect(content().string(containsString("Effective members")));
    }
}
