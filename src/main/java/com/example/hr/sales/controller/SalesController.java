package com.example.hr.sales.controller;

import com.example.hr.models.User;
import com.example.hr.sales.entity.SalesCustomer;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.service.SalesService;
import com.example.hr.service.AuthUserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SalesController {

    private final SalesService salesService;
    private final AuthUserHelper authUserHelper;

    @GetMapping({"/sales", "/admin/sales", "/manager/sales"})
    public String dashboard(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null || (user.getRole() != com.example.hr.enums.Role.ADMIN && user.getRole() != com.example.hr.enums.Role.MANAGER)) {
            return "redirect:/sales/products";
        }
        model.addAttribute("products", salesService.getProducts());
        model.addAttribute("myProducts", salesService.getMyProducts(user));
        model.addAttribute("pendingProducts", salesService.getPendingProducts());
        model.addAttribute("currentRole", user != null && user.getRole() != null ? user.getRole().name() : "USER");
        model.addAttribute("customers", salesService.getCustomers());
        model.addAttribute("orders", salesService.getOrders());
        return "sales/dashboard";
    }

    @GetMapping({"/sales/products", "/admin/sales/products", "/manager/sales/products"})
    public String products(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        model.addAttribute("products", salesService.getProducts());
        model.addAttribute("myProducts", salesService.getMyProducts(user));
        model.addAttribute("pendingProducts", salesService.getPendingProducts());
        model.addAttribute("currentRole", user != null && user.getRole() != null ? user.getRole().name() : "USER");
        return "sales/products";
    }

    @GetMapping({"/sales/products/create", "/admin/sales/products/create", "/manager/sales/products/create"})
    public String productForm(Model model) {
        model.addAttribute("product", new SalesProduct());
        model.addAttribute("editMode", false);
        return "sales/product-form";
    }

    @GetMapping({"/sales/products/{id}", "/admin/sales/products/{id}", "/manager/sales/products/{id}"})
    public String productDetail(@PathVariable Integer id, Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        SalesProduct product = salesService.getProduct(id);
        model.addAttribute("product", product);
        model.addAttribute("canManage", salesService.canManageProduct(product, user));
        model.addAttribute("currentRole", user != null && user.getRole() != null ? user.getRole().name() : "USER");
        return "sales/product-detail";
    }

    @GetMapping({"/sales/products/{id}/edit", "/admin/sales/products/{id}/edit", "/manager/sales/products/{id}/edit"})
    public String productEditForm(@PathVariable Integer id, Authentication auth, Model model, RedirectAttributes ra) {
        User user = authUserHelper.getCurrentUser(auth);
        SalesProduct product = salesService.getProduct(id);
        if (!salesService.canManageProduct(product, user)) {
            ra.addFlashAttribute("error", "Ban khong co quyen sua san pham nay");
            return "redirect:/sales/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("editMode", true);
        return "sales/product-form";
    }

    @PostMapping({"/sales/products/save", "/admin/sales/products/save", "/manager/sales/products/save"})
    public String saveProduct(@ModelAttribute SalesProduct product, Authentication auth, RedirectAttributes ra) {
        try {
            User seller = authUserHelper.getCurrentUser(auth);
            SalesProduct saved = salesService.saveProduct(product, seller);
            if ("APPROVED".equals(saved.getApprovalStatus())) {
                ra.addFlashAttribute("success", "Da luu va duyet san pham");
            } else {
                ra.addFlashAttribute("success", "Da dang san pham, dang cho Manager duyet");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the luu san pham: " + e.getMessage());
        }
        return "redirect:/sales/products";
    }

    @PostMapping({"/sales/products/{id}/update", "/admin/sales/products/{id}/update", "/manager/sales/products/{id}/update"})
    public String updateProduct(@PathVariable Integer id,
                                @ModelAttribute SalesProduct product,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            salesService.updateProduct(id, product, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da cap nhat san pham");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the cap nhat san pham: " + e.getMessage());
        }
        return "redirect:/sales/products";
    }

    @PostMapping({"/sales/products/{id}/delete", "/admin/sales/products/{id}/delete", "/manager/sales/products/{id}/delete"})
    public String deleteProduct(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            salesService.deactivateProduct(id, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da an san pham");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the an san pham: " + e.getMessage());
        }
        return "redirect:/sales/products";
    }

    @PostMapping({"/sales/products/{id}/approve", "/admin/sales/products/{id}/approve", "/manager/sales/products/{id}/approve"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String approveProduct(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            salesService.approveProduct(id, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da duyet san pham");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the duyet san pham: " + e.getMessage());
        }
        return "redirect:/sales/products";
    }

    @PostMapping({"/sales/products/{id}/reject", "/admin/sales/products/{id}/reject", "/manager/sales/products/{id}/reject"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String rejectProduct(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            salesService.rejectProduct(id, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da tu choi san pham");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tu choi san pham: " + e.getMessage());
        }
        return "redirect:/sales/products";
    }

    @GetMapping({"/sales/customers", "/admin/sales/customers", "/manager/sales/customers"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String customers(Model model) {
        model.addAttribute("customers", salesService.getCustomers());
        return "sales/customers";
    }

    @GetMapping({"/sales/customers/create", "/admin/sales/customers/create", "/manager/sales/customers/create"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String customerForm(Model model) {
        model.addAttribute("customer", new SalesCustomer());
        return "sales/customer-form";
    }

    @PostMapping({"/sales/customers/save", "/admin/sales/customers/save", "/manager/sales/customers/save"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String saveCustomer(@ModelAttribute SalesCustomer customer, RedirectAttributes ra) {
        try {
            salesService.saveCustomer(customer);
            ra.addFlashAttribute("success", "Da luu khach hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the luu khach hang: " + e.getMessage());
        }
        return "redirect:/sales/customers";
    }

    @GetMapping({"/sales/orders", "/admin/sales/orders", "/manager/sales/orders"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String orders(Model model) {
        model.addAttribute("orders", salesService.getOrders());
        return "sales/orders";
    }

    @GetMapping({"/sales/orders/create", "/admin/sales/orders/create", "/manager/sales/orders/create"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String orderForm(Model model) {
        model.addAttribute("customers", salesService.getCustomers());
        model.addAttribute("products", salesService.getProducts());
        return "sales/order-form";
    }

    @PostMapping({"/sales/orders/create", "/admin/sales/orders/create", "/manager/sales/orders/create"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createOrder(@RequestParam Integer customerId,
                              @RequestParam Integer productId,
                              @RequestParam Integer quantity,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes ra) {
        try {
            salesService.createOrder(customerId, productId, quantity, notes);
            ra.addFlashAttribute("success", "Da tao don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tao don hang: " + e.getMessage());
        }
        return "redirect:/sales/orders";
    }

    @PostMapping({"/sales/orders/{id}/status", "/admin/sales/orders/{id}/status", "/manager/sales/orders/{id}/status"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String updateStatus(@PathVariable Integer id, @RequestParam String status, RedirectAttributes ra) {
        try {
            salesService.updateOrderStatus(id, status);
            ra.addFlashAttribute("success", "Da cap nhat trang thai don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the cap nhat don hang: " + e.getMessage());
        }
        return "redirect:/sales/orders";
    }
}
