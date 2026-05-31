package com.example.hr.controllers;

import com.example.hr.enums.AssetStatus;
import com.example.hr.models.CompanyAsset;
import com.example.hr.repository.CompanyAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/assets")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCompanyAssetController {

    private final CompanyAssetRepository assetRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) AssetStatus status,
                       Model model) {
        List<CompanyAsset> assets;
        if (keyword != null && !keyword.isBlank()) {
            assets = assetRepository.searchByKeyword(keyword.trim());
        } else if (status != null) {
            assets = assetRepository.findByStatus(status);
        } else {
            assets = assetRepository.findAll();
        }

        model.addAttribute("assets", assets);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", AssetStatus.values());
        model.addAttribute("totalAssets", assetRepository.count());
        model.addAttribute("availableCount", assetRepository.countByStatus(AssetStatus.AVAILABLE));
        model.addAttribute("assignedCount", assetRepository.countByStatus(AssetStatus.ASSIGNED));
        model.addAttribute("maintenanceCount", assetRepository.countByStatus(AssetStatus.MAINTENANCE));
        return "admin/asset-list";
    }

    @GetMapping("/add")
    public String add(Model model) {
        CompanyAsset asset = new CompanyAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        model.addAttribute("asset", asset);
        model.addAttribute("statuses", AssetStatus.values());
        return "admin/asset-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return assetRepository.findById(id)
                .map(asset -> {
                    model.addAttribute("asset", asset);
                    model.addAttribute("statuses", AssetStatus.values());
                    return "admin/asset-form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Không tìm thấy tài sản.");
                    return "redirect:/admin/assets";
                });
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CompanyAsset asset, RedirectAttributes ra) {
        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.AVAILABLE);
        }
        if (asset.getCreatedAt() == null) {
            asset.setCreatedAt(LocalDateTime.now());
        }
        if (asset.getPurchasePrice() == null) {
            asset.setPurchasePrice(BigDecimal.ZERO);
        }
        if (asset.getCurrentValue() == null) {
            asset.setCurrentValue(asset.getPurchasePrice());
        }

        assetRepository.save(asset);
        ra.addFlashAttribute("success", "Đã lưu tài sản thành công.");
        return "redirect:/admin/assets";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (assetRepository.existsById(id)) {
            assetRepository.deleteById(id);
            ra.addFlashAttribute("success", "Đã xóa tài sản.");
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy tài sản cần xóa.");
        }
        return "redirect:/admin/assets";
    }

    @GetMapping({"/assign", "/assignments"})
    public String assignmentPlaceholder(RedirectAttributes ra) {
        ra.addFlashAttribute("success", "Phần bàn giao tài sản sẽ được đồng bộ ở màn hình tài sản mới.");
        return "redirect:/admin/assets";
    }
}
