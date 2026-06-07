package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocumentDTO {

    private Long id;

    @NotNull(message = "User ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer userId;

    @NotBlank(message = "Loáº¡i tÃ i liá»‡u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String documentType;

    @NotBlank(message = "TÃªn file khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String fileName;

    private String fileUrl;
    private String fileSize;
    private String mimeType;
    private String description;
    private Boolean isConfidential;
}


