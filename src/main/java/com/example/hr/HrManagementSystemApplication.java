package com.example.hr;


import com.example.hr.department.entity.Department;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication; // Dòng này cực kỳ quan trọng
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EntityScan({
        "com.example.hr.models",
        "com.example.hr.attendance.entity",
        "com.example.hr.leave.entity",
        "com.example.hr.payment.entity",
        "com.example.hr.payroll.entity",
        "com.example.hr.sales.entity",
        "com.example.hr.training.entity",
        "com.example.hr.recruitment.entity",
        "com.example.hr.engagement.entity",
        "com.example.hr.task.entity",
        "com.example.hr.system.entity",
        "com.example.hr.backup.entity",
        "com.example.hr.department.entity"
})
@EnableJpaRepositories({
        "com.example.hr.repository",
        "com.example.hr.attendance.repository",
        "com.example.hr.leave.repository",
        "com.example.hr.payment.repository",
        "com.example.hr.payroll.repository",
        "com.example.hr.sales.repository",
        "com.example.hr.training.repository",
        "com.example.hr.recruitment.repository",
        "com.example.hr.engagement.repository",
        "com.example.hr.task.repository",
        "com.example.hr.system.repository",
        "com.example.hr.backup.repository",
        "com.example.hr.department.repository",
        "com.example.hr.user.repository"
})
@EnableScheduling
@SpringBootApplication
@org.springframework.cache.annotation.EnableCaching
public class HrManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrManagementSystemApplication.class, args);
         System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }

}
