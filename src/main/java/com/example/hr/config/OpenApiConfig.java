package com.example.hr.config;


import com.example.hr.payment.entity.Payment;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cấu hình Swagger / OpenAPI 3 documentation.
 * Truy cập tại: /swagger-ui.html hoặc /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${payment.gateway.base-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI hrmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HR Management System API")
                        .description("""
                                **HRMS REST API Documentation**
                                
                                Hệ thống quản lý nhân sự tích hợp:
                                - 👤 Quản lý người dùng & phòng ban
                                - 📅 Chấm công & Xin nghỉ phép
                                - 💰 Lương thưởng & Hợp đồng
                                - 📊 KPI & Đánh giá hiệu suất
                                - 🎓 Đào tạo & Phát triển
                                - 📦 Quản lý tài sản
                                - 💳 Thanh toán (MoMo / VNPay)
                                - 🔥 Cache Management (Redis)
                                - ☁️ Cloud Storage (S3/Drive/Cloudinary)
                                
                                **Authentication:** Form-based login (session-cookie)
                                """)
                        .version("v2.0.0")
                        .contact(new Contact()
                                .name("HRMS Dev Team")
                                .email("admin@hrms.com")
                                .url(serverUrl))
                        .license(new License()
                                .name("Private — Internal Use Only")))
                .servers(List.of(
                        new Server().url(serverUrl).description("Local / Development"),
                        new Server().url("https://hrms.yourdomain.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("JSESSIONID")
                                        .description("Session cookie từ form login (/login)")));
    }
}
