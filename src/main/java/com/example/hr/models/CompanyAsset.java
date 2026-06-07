package com.example.hr.models;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import com.example.hr.enums.AssetStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity tÃ i sáº£n cÃ´ng ty (laptop, monitor, bÃ n, Ä‘iá»‡n thoáº¡i, v.v.).
 */
@Entity
@Table(name = "company_asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "asset_name", nullable = false, length = 200)
    private String assetName;

    @Column(name = "asset_code", unique = true, length = 50)
    private String assetCode;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status = AssetStatus.AVAILABLE;

    @Column(length = 200)
    private String location;

    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Business Logic ---

    /**
     * TÃ­nh giÃ¡ trá»‹ kháº¥u hao (straight-line depreciation, 3 nÄƒm).
     */
    public BigDecimal calculateDepreciation(int usefulLifeYears) {
        if (purchasePrice == null || purchaseDate == null || usefulLifeYears <= 0) {
            return BigDecimal.ZERO;
        }
        long monthsUsed = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
        long totalMonths = (long) usefulLifeYears * 12;
        if (monthsUsed >= totalMonths) {
            return purchasePrice; // fully depreciated
        }
        return purchasePrice.multiply(BigDecimal.valueOf(monthsUsed))
                .divide(BigDecimal.valueOf(totalMonths), 0, RoundingMode.HALF_UP);
    }

    /**
     * Cáº­p nháº­t giÃ¡ trá»‹ hiá»‡n táº¡i (current value = purchase - depreciation).
     */
    public void updateCurrentValue(int usefulLifeYears) {
        BigDecimal dep = calculateDepreciation(usefulLifeYears);
        this.currentValue = purchasePrice.subtract(dep).max(BigDecimal.ZERO);
    }

    /**
     * Kiá»ƒm tra báº£o hÃ nh cÃ²n hiá»‡u lá»±c.
     */
    public boolean isUnderWarranty() {
        return warrantyExpiry != null && !warrantyExpiry.isBefore(LocalDate.now());
    }

    /**
     * Kiá»ƒm tra báº£o hÃ nh sáº¯p háº¿t (trong 30 ngÃ y).
     */
    public boolean isWarrantyExpiringSoon() {
        if (warrantyExpiry == null) return false;
        LocalDate today = LocalDate.now();
        return warrantyExpiry.isAfter(today)
                && warrantyExpiry.isBefore(today.plusDays(31));
    }

    /**
     * Kiá»ƒm tra tÃ i sáº£n cÃ³ sáºµn sÃ ng Ä‘á»ƒ giao.
     */
    public boolean isAvailableForAssignment() {
        return status == AssetStatus.AVAILABLE;
    }

    /**
     * ÄÃ¡nh dáº¥u Ä‘Ã£ giao.
     */
    public void markAssigned() {
        this.status = AssetStatus.ASSIGNED;
    }

    /**
     * ÄÃ¡nh dáº¥u sáºµn sÃ ng (khi tráº£ láº¡i).
     */
    public void markAvailable() {
        this.status = AssetStatus.AVAILABLE;
    }

    /**
     * TÃ­nh tuá»•i tÃ i sáº£n (nÄƒm).
     */
    public double getAssetAgeYears() {
        if (purchaseDate == null) return 0;
        long days = ChronoUnit.DAYS.between(purchaseDate, LocalDate.now());
        return days / 365.25;
    }

    /**
     * Láº¥y icon theo loáº¡i tÃ i sáº£n.
     */
    public String getCategoryIcon() {
        if (category == null) return "bi-box";
        return switch (category.toUpperCase()) {
            case "LAPTOP" -> "bi-laptop";
            case "MONITOR" -> "bi-display";
            case "PHONE" -> "bi-phone";
            case "FURNITURE" -> "bi-building";
            case "PRINTER" -> "bi-printer";
            case "NETWORK" -> "bi-router";
            default -> "bi-box";
        };
    }

    /**
     * TÃ­nh tá»· lá»‡ kháº¥u hao (%).
     */
    public double getDepreciationPercentage(int usefulLifeYears) {
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal dep = calculateDepreciation(usefulLifeYears);
        return dep.divide(purchasePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


