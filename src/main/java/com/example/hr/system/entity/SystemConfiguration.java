package com.example.hr.system.entity;

import com.example.hr.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category", nullable = false, length = 100)
    private String category; // GENERAL, EMAIL, SMS, SECURITY, INTEGRATION, NOTIFICATION

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "data_type", length = 50)
    private String dataType; // STRING, INTEGER, BOOLEAN, JSON, ENCRYPTED

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted = false;

    @Column(name = "is_editable")
    private Boolean isEditable = true;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Column(name = "validation_rule", length = 500)
    private String validationRule;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    // Getter/setter methods for compatibility
    public String getKey() {
        return this.configKey;
    }

    public void setKey(String key) {
        this.configKey = key;
    }

    public String getValue() {
        return this.configValue;
    }

    public void setValue(String value) {
        this.configValue = value;
    }
}


