package com.example.hr.dto;

import com.example.hr.enums.WarningLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWarningDTO {

    private Integer id;

    @NotNull(message = "User ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer userId;

    @NotNull(message = "Issued By khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer issuedById;

    @NotNull(message = "Má»©c cáº£nh cÃ¡o khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private WarningLevel warningLevel;

    @NotBlank(message = "LÃ½ do khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String reason;

    private String description;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private String attachmentUrl;
}


