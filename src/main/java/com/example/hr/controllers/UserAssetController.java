package com.example.hr.controllers;

import com.example.hr.models.AssetAssignment;
import com.example.hr.models.User;
import com.example.hr.repository.AssetAssignmentRepository;
import com.example.hr.service.AuthUserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user1/assets")
@PreAuthorize("isAuthenticated()")
public class UserAssetController {

    @Autowired private AssetAssignmentRepository assetAssignmentRepository;
    @Autowired private AuthUserHelper authUserHelper;

    @GetMapping
    public String myAssets(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        // TODO: Update with new asset model
        List<AssetAssignment> myAssets = new ArrayList<>(); // assetAssignmentRepository.findByUserOrderByAssignedDateDesc(user);
        
        model.addAttribute("user", user);
        model.addAttribute("myAssets", myAssets);
        return "user1/my-assets";
    }
}

