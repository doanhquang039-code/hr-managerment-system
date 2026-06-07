package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetAssignmentDTO {

    private Integer id;

    @NotNull(message = "Asset ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer assetId;

    @NotNull(message = "User ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer userId;

    @NotNull(message = "NgÃ y giao khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate assignedDate;

    private LocalDate expectedReturn;
    private Integer assignedById;
    private String conditionOnAssign;
    private String notes;

    // Cho tráº£ tÃ i sáº£n
    private String conditionOnReturn;
}


