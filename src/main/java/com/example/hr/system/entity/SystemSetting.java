package com.example.hr.system.entity;

import jakarta.persistence.PreUpdate;

import jakarta.persistence.PrePersist;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_setting")
public class SystemSetting {

    @Id
    @Column(name = "setting_key", length = 100, nullable = false)
    private String settingKey;

    @Column(name = "setting_value", length = 500)
    private String settingValue;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

