package com.example.hr.service;

import com.example.hr.models.Department;
import com.example.hr.models.JobPosition;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        
        // 1. Xác định đang login bằng mạng xã hội nào (google, facebook, zalo, tiktok)
        String clientName = userRequest.getClientRegistration().getRegistrationId();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // 2. Xử lý đặc biệt cho Zalo hoặc TikTok (nếu họ không trả về email)
        if ("zalo".equals(clientName) || email == null) {
            String id = oAuth2User.getName(); // Lấy ID duy nhất của MXH đó
            email = id + "@" + clientName + ".com"; // Tạo email giả định: 12345@zalo.com
        }
        
        if (name == null) {
            name = oAuth2User.getAttribute("display_name"); // Dành cho TikTok
        }

        // 3. Gọi hàm lưu hoặc cập nhật vào Database
        saveOrUpdateUser(email, name, picture);

        return oAuth2User;
    }

    // Hàm phụ trợ để xử lý lưu Database cho đỡ rối code ở trên
    private void saveOrUpdateUser(String email, String name, String picture) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setProfileImage(picture);
            userRepository.save(existingUser);
            System.out.println("--- Cập nhật thành viên: " + email);
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : "Social User");
            newUser.setProfileImage(picture);
            newUser.setUsername(email);
            newUser.setPassword(""); 
            newUser.setRole(Role.USER);
            newUser.setStatus(UserStatus.ACTIVE);

            // Gán giá trị mặc định tránh lỗi NOT NULL trong MySQL
            Department defaultDept = departmentRepository.findById(1).orElse(null);
            JobPosition defaultPos = jobPositionRepository.findById(1).orElse(null);
            newUser.setDepartment(defaultDept);
            newUser.setPosition(defaultPos);

            userRepository.save(newUser);
            System.out.println("--- Đã đăng ký thành viên mới: " + email);
        }
    }
}