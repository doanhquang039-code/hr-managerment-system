package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAssetDTO {

    private Integer id;

    @NotBlank(message = "TÃªn tÃ i sáº£n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String assetName;

    private String assetCode;

    @NotBlank(message = "Danh má»¥c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String category;

    private String serialNumber;
    private String description;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private String location;
    private LocalDate warrantyExpiry;
}


