package com.example.hr.recruitment.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {
    JobPostingController.class,
    CandidateController.class,
    InterviewController.class,
    RecruitmentController.class
})
public class HiringPathAdvice {

    @ModelAttribute("hiringPath")
    public String getHiringPath(HttpServletRequest request) {
        return HiringPathHelper.getRedirectPrefix(request);
    }
}
