package com.example.hr.service;

import com.example.hr.department.entity.Department;
import com.example.hr.recruitment.entity.JobPosition;
import com.example.hr.models.User;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.recruitment.repository.JobPositionRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 1. XÃ¡c Ä‘á»‹nh Ä‘ang login báº±ng máº¡ng xÃ£ há»™i nÃ o (google, facebook, zalo, tiktok)
        String clientName = userRequest.getClientRegistration().getRegistrationId();
        
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("preferred_username");
        }
        if (email == null) {
            email = oAuth2User.getAttribute("mail");
        }
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // 2. Xá»­ lÃ½ Ä‘áº·c biá»‡t cho Zalo hoáº·c TikTok (náº¿u há» khÃ´ng tráº£ vá» email)
        if ("zalo".equals(clientName) || email == null) {
            String id = oAuth2User.getName(); // Láº¥y ID duy nháº¥t cá»§a MXH Ä‘Ã³
            email = id + "@" + clientName + ".com"; // Táº¡o email giáº£ Ä‘á»‹nh: 12345@zalo.com
        }
        
        if (name == null) {
            name = oAuth2User.getAttribute("display_name"); // DÃ nh cho TikTok
        }

        // 3. Gá»i hÃ m lÆ°u hoáº·c cáº­p nháº­t vÃ o Database
        User user = saveOrUpdateUser(email, name, picture);

        Set<GrantedAuthority> authorities = new LinkedHashSet<>(oAuth2User.getAuthorities());
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        if (userNameAttributeName == null || userNameAttributeName.isBlank()) {
            userNameAttributeName = "sub";
        }

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), userNameAttributeName);
    }

    // HÃ m phá»¥ trá»£ Ä‘á»ƒ xá»­ lÃ½ lÆ°u Database cho Ä‘á»¡ rá»‘i code á»Ÿ trÃªn
    public User saveOrUpdateUser(String email, String name, String picture) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setProfileImage(picture);
            User savedUser = userRepository.save(existingUser);
            System.out.println("--- Cáº­p nháº­t thÃ nh viÃªn: " + email);
            return savedUser;
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : "Social User");
            newUser.setProfileImage(picture);
            newUser.setUsername(email);
            newUser.setPassword(""); 
            newUser.setRole(Role.USER);
            newUser.setStatus(UserStatus.ACTIVE);

            // GÃ¡n giÃ¡ trá»‹ máº·c Ä‘á»‹nh trÃ¡nh lá»—i NOT NULL trong MySQL
            Department defaultDept = departmentRepository.findById(1).orElse(null);
            JobPosition defaultPos = jobPositionRepository.findById(1).orElse(null);
            newUser.setDepartment(defaultDept);
            newUser.setPosition(defaultPos);

            User savedUser = userRepository.save(newUser);
            System.out.println("--- ÄÃ£ Ä‘Äƒng kÃ½ thÃ nh viÃªn má»›i: " + email);
            return savedUser;
        }
    }
}

