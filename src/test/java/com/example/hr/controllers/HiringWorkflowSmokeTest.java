package com.example.hr.controllers;

import com.example.hr.repository.JobPostingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class HiringWorkflowSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void jobPostingListRendersWithCombinedFilters() throws Exception {
        mockMvc.perform(get("/hiring/jobs")
                        .param("search", "a")
                        .param("status", "ACTIVE")
                        .param("employmentType", "FULL_TIME")
                        .param("sortBy", "applicationsCount")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("hiring/jobs/list"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void jobPostingCreateAndClosingSoonPagesRender() throws Exception {
        mockMvc.perform(get("/hiring/jobs/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("hiring/jobs/create"));

        mockMvc.perform(get("/hiring/jobs/closing-soon"))
                .andExpect(status().isOk())
                .andExpect(view().name("hiring/jobs/closing-soon"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void jobPostingViewAndEditPagesRenderWhenDataExists() throws Exception {
        var job = jobPostingRepository.findAll().stream().findFirst();
        assumeTrue(job.isPresent(), "Need at least one job posting to smoke test view/edit pages");

        mockMvc.perform(get("/hiring/jobs/{id}", job.get().getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("hiring/jobs/view"));

        mockMvc.perform(get("/hiring/jobs/{id}/edit", job.get().getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("hiring/jobs/edit"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void interviewListRendersWithSearchTypeAndSort() throws Exception {
        mockMvc.perform(get("/hiring/interviews")
                        .param("search", "a")
                        .param("type", "TECHNICAL")
                        .param("sortBy", "overallScore")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("hiring/interviews/list"));
    }
}
