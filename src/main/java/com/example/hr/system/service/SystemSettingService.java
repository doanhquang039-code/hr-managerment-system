package com.example.hr.system.service;

import com.example.hr.system.entity.SystemSetting;
import com.example.hr.system.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    public SystemSettingService(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @PostConstruct
    public void initDefaultSettings() {
        addDefaultSetting("COMPANY_NAME", "CÃ´ng ty CP CÃ´ng nghá»‡ HRMS", "TÃªn cÃ´ng ty hiá»ƒn thá»‹ trÃªn bÃ¡o cÃ¡o");
        addDefaultSetting("WORKING_HOURS_PER_DAY", "8", "Sá»‘ giá» lÃ m viá»‡c tiÃªu chuáº©n má»—i ngÃ y");
        addDefaultSetting("MAX_LEAVE_CARRYOVER", "5", "Sá»‘ ngÃ y phÃ©p tá»‘i Ä‘a Ä‘Æ°á»£c chuyá»ƒn sang nÄƒm sau");
        addDefaultSetting("ENABLE_AUTO_ATTENDANCE", "true", "Tá»± Ä‘á»™ng cháº¥m cÃ´ng váº¯ng máº·t náº¿u khÃ´ng check-in (true/false)");
        addDefaultSetting("LOGIN_VERIFICATION_ENABLED", "true", "Báº­t mÃ£ xÃ¡c nháº­n khi Ä‘Äƒng nháº­p (true/false)");
        addDefaultSetting("LOGIN_VERIFICATION_CODE_LENGTH", "5", "Äá»™ dÃ i mÃ£ xÃ¡c nháº­n Ä‘Äƒng nháº­p, tá»« 4 Ä‘áº¿n 8 kÃ½ tá»±");
        addDefaultSetting("LOGIN_VERIFICATION_CHARACTERS", "ABCDEFGHJKLMNPQRSTUVWXYZ23456789", "Bá»™ kÃ½ tá»± dÃ¹ng Ä‘á»ƒ sinh mÃ£ xÃ¡c nháº­n Ä‘Äƒng nháº­p");
    }

    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    public String getValue(String key, String defaultValue) {
        return settingRepository.findById(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getValue(key, String.valueOf(defaultValue)));
    }

    public int getInt(String key, int defaultValue, int minValue, int maxValue) {
        try {
            int value = Integer.parseInt(getValue(key, String.valueOf(defaultValue)).trim());
            return Math.max(minValue, Math.min(maxValue, value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public void updateSetting(String key, String value) {
        SystemSetting setting = settingRepository.findById(key).orElse(new SystemSetting());
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setUpdatedAt(LocalDateTime.now());
        settingRepository.save(setting);
    }

    public void addSetting(String key, String value, String description) {
        if (!settingRepository.existsById(key)) {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setDescription(description);
            setting.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(setting);
        }
    }

    private void addDefaultSetting(String key, String value, String description) {
        if (!settingRepository.existsById(key)) {
            addSetting(key, value, description);
        }
    }
}


