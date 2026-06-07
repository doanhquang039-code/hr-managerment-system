package com.example.hr.system.service;

import com.example.hr.system.entity.SystemConfiguration;
import com.example.hr.models.User;
import com.example.hr.system.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemConfigurationService {

    private final SystemConfigurationRepository configRepository;

    /**
     * Láº¥y giÃ¡ trá»‹ config theo key
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getConfigValue(String configKey) {
        return configRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(null);
    }

    /**
     * Láº¥y giÃ¡ trá»‹ config vá»›i default value
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getConfigValue(String configKey, String defaultValue) {
        return configRepository.findByConfigKey(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(defaultValue);
    }

    /**
     * Láº¥y config object
     */
    public Optional<SystemConfiguration> getConfig(String configKey) {
        return configRepository.findByConfigKey(configKey);
    }

    /**
     * Láº¥y táº¥t cáº£ configs theo category
     */
    public List<SystemConfiguration> getConfigsByCategory(String category) {
        return configRepository.findByCategoryOrderByDisplayOrderAsc(category);
    }

    /**
     * Láº¥y táº¥t cáº£ visible configs
     */
    public List<SystemConfiguration> getAllVisibleConfigs() {
        return configRepository.findByIsVisibleTrueOrderByCategoryAscDisplayOrderAsc();
    }

    /**
     * Láº¥y táº¥t cáº£ categories
     */
    public List<String> getAllCategories() {
        return configRepository.findAllCategories();
    }

    /**
     * Táº¡o hoáº·c cáº­p nháº­t config
     */
    @CacheEvict(value = "systemConfig", key = "#config.configKey")
    public SystemConfiguration saveConfig(SystemConfiguration config, User updatedBy) {
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);
        
        // Náº¿u lÃ  encrypted, mÃ£ hÃ³a value
        if (Boolean.TRUE.equals(config.getIsEncrypted())) {
            // TODO: Implement encryption
            log.warn("Encryption not implemented yet for config: {}", config.getConfigKey());
        }
        
        return configRepository.save(config);
    }

    /**
     * Cáº­p nháº­t giÃ¡ trá»‹ config
     */
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void updateConfigValue(String configKey, String newValue, User updatedBy) {
        SystemConfiguration config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("Config not found: " + configKey));
        
        config.setConfigValue(newValue);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);
        
        configRepository.save(config);
        log.info("Updated config {} by user {}", configKey, updatedBy.getUsername());
    }

    /**
     * XÃ³a config
     */
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void deleteConfig(String configKey) {
        configRepository.findByConfigKey(configKey)
                .ifPresent(config -> {
                    configRepository.delete(config);
                    log.info("Deleted config: {}", configKey);
                });
    }

    /**
     * TÃ¬m kiáº¿m configs
     */
    public List<SystemConfiguration> searchConfigs(String keyword) {
        return configRepository.searchConfigurations(keyword);
    }

    /**
     * Reset config vá» default value
     */
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void resetToDefault(String configKey, User updatedBy) {
        SystemConfiguration config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("Config not found: " + configKey));
        
        config.setConfigValue(config.getDefaultValue());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);
        
        configRepository.save(config);
        log.info("Reset config {} to default by user {}", configKey, updatedBy.getUsername());
    }

    /**
     * Kiá»ƒm tra config cÃ³ tá»“n táº¡i khÃ´ng
     */
    public boolean configExists(String configKey) {
        return configRepository.existsByConfigKey(configKey);
    }

    /**
     * Láº¥y sá»‘ lÆ°á»£ng configs theo category
     */
    public long countByCategory(String category) {
        return configRepository.countByCategory(category);
    }

    /**
     * Clear cache
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void clearCache() {
        log.info("Cleared system configuration cache");
    }

    /**
     * Get all configurations for admin management
     */
    public List<SystemConfiguration> getAllConfigurations() {
        return configRepository.findAll();
    }

    /**
     * Get configurations by category with statistics
     */
    public List<SystemConfiguration> getConfigurationsByCategory() {
        return configRepository.getConfigurationsByCategory();
    }

    /**
     * Get configurations by specific category
     */
    public List<SystemConfiguration> getConfigurationsByCategory(String category) {
        return configRepository.findByCategoryOrderByDisplayOrderAsc(category);
    }

    /**
     * Update configuration by key and value
     */
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public SystemConfiguration updateConfiguration(String configKey, String configValue) {
        SystemConfiguration config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("Configuration not found: " + configKey));
        
        config.setConfigValue(configValue);
        config.setUpdatedAt(LocalDateTime.now());
        
        return configRepository.save(config);
    }

    /**
     * Create new configuration
     */
    public SystemConfiguration createConfiguration(SystemConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return configRepository.save(config);
    }

    /**
     * Delete configuration by ID
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void deleteConfiguration(Integer id) {
        configRepository.deleteById(id);
    }

    /**
     * Reset all configurations to defaults
     */
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void resetToDefaults() {
        List<SystemConfiguration> configs = configRepository.findAll();
        for (SystemConfiguration config : configs) {
            if (config.getDefaultValue() != null) {
                config.setConfigValue(config.getDefaultValue());
                config.setUpdatedAt(LocalDateTime.now());
            }
        }
        configRepository.saveAll(configs);
        log.info("Reset all configurations to defaults");
    }
}


