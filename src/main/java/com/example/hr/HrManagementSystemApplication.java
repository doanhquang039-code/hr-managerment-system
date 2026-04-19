package com.example.hr;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication; // Dòng này cực kỳ quan trọng
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.example.hr.repository.UserRepository;

@EntityScan("com.example.hr.models")
@EnableJpaRepositories("com.example.hr.repository")
@EnableScheduling
@SpringBootApplication
public class HrManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrManagementSystemApplication.class, args);
         System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }

}