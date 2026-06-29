package com.example.hr.sales.controller;

import com.example.hr.models.User;
import com.example.hr.payment.dto.CartDto;
import com.example.hr.payment.service.MarketplacePaymentService;
import com.example.hr.sales.entity.OrderChat;
import com.example.hr.sales.entity.ProductReview;
import com.example.hr.sales.entity.SalesCustomer;
import com.example.hr.sales.entity.SalesOrder;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.service.SalesService;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.CloudStorageFacade;
import com.example.hr.service.QRCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Sản phẩm, Khách hàng, Đơn hàng Sales.
 *
 * Phân chia trách nhiệm:
 *  - SalesController          → CRUD sản phẩm, khách hàng, đơn hàng
 *  - MarketplacePaymentController → Giỏ hàng + Checkout (trong package payment)
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SalesController {

    private final SalesService salesService;
    private final AuthUserHelper authUserHelper;
    private final MarketplacePaymentService marketplacePaymentService;
    private final CloudStorageFacade cloudStorageFacade;
    private final QRCodeService qrCodeService;

    private static final String CART_SESSION_KEY = "salesCart";

    // ==================== Helper giỏ hàng ====================
    private CartDto getCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    // ==================== DASHBOARD ====================

    @GetMapping({"/sales", "/admin/sales", "/manager/sales"})
    public String dashboard(Authentication auth, Model model, HttpSession session) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return "redirect:/sales/products";
        }
        String roleName = normalizeRole(user.getEffectiveRoleName());
        if (!"ADMIN".equals(roleName) && !"MANAGER".equals(roleName)) {
            return "redirect:/sales/products";
        }
        model.addAttribute("products", salesService.getProducts());
        model.addAttribute("myProducts", salesService.getMyProducts(user));
        model.addAttribute("pendingProducts", salesService.getPendingProducts());
        model.addAttribute("currentRole", roleName);
        model.addAttribute("customers", salesService.getCustomers());
        model.addAttribute("orders", salesService.getOrders());
        model.addAttribute("cartCount", getCart(session).getTotalItems());
        return "sales/dashboard";
    }

    // ==================== PRODUCTS ====================

    @GetMapping({"/sales/products", "/admin/sales/products", "/manager/sales/products"})
    public String products(@RequestParam(required = false) String search, Authentication auth, Model model, HttpSession session) {
        User user = authUserHelper.getCurrentUser(auth);
        String roleName = user != null ? normalizeRole(user.getEffectiveRoleName()) : "USER";
        boolean canApproveSalesProducts = canApproveSales(roleName);
        java.util.List<SalesProduct> productsList;
        if (search != null && !search.isBlank()) {
            String normalizedSearch = search.trim().toLowerCase();
            productsList = (canApproveSalesProducts ? salesService.getProductsForApprovalView() : salesService.searchProducts(search))
                    .stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(normalizedSearch))
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(normalizedSearch)))
                    .toList();
            model.addAttribute("searchQuery", search);
        } else {
            productsList = canApproveSalesProducts ? salesService.getProductsForApprovalView() : salesService.getProducts();
        }
        model.addAttribute("products", productsList);
        model.addAttribute("myProducts", salesService.getMyProducts(user));
        java.util.List<SalesProduct> pendingProducts = salesService.getPendingProducts();
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("pendingProductCount", pendingProducts.size());
        model.addAttribute("currentRole", roleName);
        model.addAttribute("canApproveSalesProducts", canApproveSalesProducts);
        model.addAttribute("cartCount", getCart(session).getTotalItems());
        return "sales/products";
    }

    @GetMapping({"/sales/products/create", "/admin/sales/products/create", "/manager/sales/products/create"})
    public String productForm(Model model) {
        model.addAttribute("product", new SalesProduct());
        model.addAttribute("editMode", false);
        return "sales/product-form";
    }

    @GetMapping({"/sales/products/{id}", "/admin/sales/products/{id}", "/manager/sales/products/{id}"})
    public String productDetail(@PathVariable Integer id, Authentication auth, Model model, HttpSession session) {
        User user = authUserHelper.getCurrentUser(auth);
        SalesProduct product = salesService.getProduct(id);
        model.addAttribute("product", product);
        model.addAttribute("canManage", salesService.canManageProduct(product, user));
        String roleName = user != null ? normalizeRole(user.getEffectiveRoleName()) : "USER";
        model.addAttribute("currentRole", roleName);
        model.addAttribute("canApproveSalesProducts", canApproveSales(roleName));
        model.addAttribute("cartCount", getCart(session).getTotalItems());
        // VietQR tự động — delegate cho MarketplacePaymentService
        model.addAttribute("vietQrUrl", marketplacePaymentService.generateVietQrUrl(product));

        // Sinh QR Code chia sẻ sản phẩm qua QRCodeService
        try {
            String productUrl = "http://localhost:8080/sales/products/" + id;
            String shareQrCode = qrCodeService.generateQRCodeImage(productUrl, 200, 200);
            model.addAttribute("shareQrCode", shareQrCode);
        } catch (Exception e) {
            // Log lỗi
        }

        // Reviews & Ratings
        model.addAttribute("reviews", salesService.getProductReviews(id));
        model.addAttribute("avgRating", salesService.getAverageRating(id));

        return "sales/product-detail";
    }

    @PostMapping({"/sales/products/{id}/review", "/admin/sales/products/{id}/review", "/manager/sales/products/{id}/review"})
    public String addProductReview(@PathVariable Integer id, @RequestParam Integer rating,
                                   @RequestParam(required = false) String comment,
                                   Authentication auth, RedirectAttributes ra) {
        try {
            User buyer = authUserHelper.getCurrentUser(auth);
            salesService.addProductReview(id, buyer, rating, comment);
            ra.addFlashAttribute("success", "🎉 Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể gửi đánh giá: " + e.getMessage());
        }
        return "redirect:/sales/products/" + id;
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
            ra.addFlashAttribute("success", "APPROVED".equals(saved.getApprovalStatus())
                    ? "Da luu va duyet san pham" : "Da dang san pham, dang cho Manager duyet");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the luu san pham: " + e.getMessage());
        }
        return "redirect:/sales/products";
    }

    @PostMapping({"/sales/products/{id}/update", "/admin/sales/products/{id}/update", "/manager/sales/products/{id}/update"})
    public String updateProduct(@PathVariable Integer id, @ModelAttribute SalesProduct product,
                                Authentication auth, RedirectAttributes ra) {
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

    // ==================== CUSTOMERS ====================

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

    // ==================== ORDERS ====================

    @GetMapping({"/sales/orders", "/admin/sales/orders", "/manager/sales/orders"})
    public String orders(@RequestParam(required = false) String status, Authentication auth, Model model, HttpSession session) {
        User user = authUserHelper.getCurrentUser(auth);
        boolean pendingOnly = "PENDING".equalsIgnoreCase(status);
        model.addAttribute("orders", pendingOnly ? salesService.getPendingOrders() : salesService.getOrders());
        model.addAttribute("pendingOrders", salesService.getPendingOrders());
        model.addAttribute("selectedStatus", pendingOnly ? "PENDING" : "");
        String roleName = user != null ? normalizeRole(user.getEffectiveRoleName()) : "USER";
        model.addAttribute("currentRole", roleName);
        model.addAttribute("canApproveSalesProducts", canApproveSales(roleName));
        model.addAttribute("cartCount", getCart(session).getTotalItems());
        return "sales/orders";
    }

    @GetMapping({"/sales/orders/pending", "/admin/sales/orders/pending", "/manager/sales/orders/pending"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String pendingOrders(Authentication auth, Model model, HttpSession session) {
        return orders("PENDING", auth, model, session);
    }

    @GetMapping({"/sales/orders/create", "/admin/sales/orders/create", "/manager/sales/orders/create"})
    public String orderForm(Model model) {
        model.addAttribute("customers", salesService.getCustomers());
        model.addAttribute("products", salesService.getProducts());
        return "sales/order-form";
    }

    @PostMapping({"/sales/orders/create", "/admin/sales/orders/create", "/manager/sales/orders/create"})
    public String createOrder(@RequestParam Integer customerId, @RequestParam Integer productId,
                              @RequestParam Integer quantity, @RequestParam(required = false) String notes,
                              Authentication auth, RedirectAttributes ra) {
        try {
            salesService.createOrder(customerId, productId, quantity, notes, authUserHelper.getCurrentUser(auth));
            ra.addFlashAttribute("success", "Da tao don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tao don hang: " + e.getMessage());
        }
        return "redirect:/sales/orders";
    }

    @PostMapping({"/sales/orders/{id}/status", "/admin/sales/orders/{id}/status", "/manager/sales/orders/{id}/status"})
    public String updateStatus(@PathVariable Integer id, @RequestParam String status, RedirectAttributes ra) {
        try {
            salesService.updateOrderStatus(id, status);
            ra.addFlashAttribute("success", "Da cap nhat trang thai don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the cap nhat don hang: " + e.getMessage());
        }
        return "redirect:/sales/orders";
    }

    @PostMapping({"/sales/orders/{id}/approve", "/admin/sales/orders/{id}/approve", "/manager/sales/orders/{id}/approve"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String approveOrder(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            salesService.approveOrder(id);
            ra.addFlashAttribute("success", "Da duyet don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the duyet don hang: " + e.getMessage());
        }
        return "redirect:/sales/orders?status=PENDING";
    }

    @PostMapping({"/sales/orders/{id}/reject", "/admin/sales/orders/{id}/reject", "/manager/sales/orders/{id}/reject"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String rejectOrder(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            salesService.rejectOrder(id);
            ra.addFlashAttribute("success", "Da tu choi don hang");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tu choi don hang: " + e.getMessage());
        }
        return "redirect:/sales/orders?status=PENDING";
    }

    @PostMapping({"/sales/orders/{id}/upload-proof", "/admin/sales/orders/{id}/upload-proof", "/manager/sales/orders/{id}/upload-proof"})
    public String uploadPaymentProof(@PathVariable Integer id, @RequestParam("file") MultipartFile file,
                                     Authentication auth, RedirectAttributes ra) {
        try {
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn tệp ảnh minh chứng thanh toán");
                return "redirect:/sales/orders";
            }
            User currentUser = authUserHelper.getCurrentUser(auth);
            byte[] bytes = file.getBytes();
            String fileName = "proof_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String mimeType = file.getContentType();

            java.util.Map<String, String> urls = cloudStorageFacade.uploadEmployeeDocument(bytes, fileName, mimeType, currentUser.getId());
            String driveUrl = urls.get("drive");

            if (driveUrl == null || driveUrl.isBlank()) {
                ra.addFlashAttribute("error", "Không thể upload ảnh lên Google Drive, vui lòng cấu hình tài khoản Google Drive.");
                return "redirect:/sales/orders";
            }

            SalesOrder order = salesService.getOrder(id);
            order.setPaymentProofUrl(driveUrl);
            salesService.saveOrder(order);

            ra.addFlashAttribute("success", "🎉 Upload ảnh minh chứng thanh toán thành công lên Google Drive!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi upload ảnh minh chứng: " + e.getMessage());
        }
        return "redirect:/sales/orders";
    }

    @GetMapping({"/sales/orders/{id}", "/admin/sales/orders/{id}", "/manager/sales/orders/{id}"})
    public String orderDetail(@PathVariable Integer id, Authentication auth, Model model, HttpSession session) {
        User user = authUserHelper.getCurrentUser(auth);
        SalesOrder order = salesService.getOrder(id);
        
        model.addAttribute("order", order);
        model.addAttribute("currentUser", user);
        model.addAttribute("chats", salesService.getOrderChats(id));
        String roleName = user != null ? normalizeRole(user.getEffectiveRoleName()) : "USER";
        model.addAttribute("currentRole", roleName);
        model.addAttribute("canApproveSalesProducts", canApproveSales(roleName));
        model.addAttribute("cartCount", getCart(session).getTotalItems());
        
        return "sales/order-detail";
    }

    private String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return "USER";
        return roleName.trim().replaceFirst("^ROLE_", "").toUpperCase();
    }

    private boolean canApproveSales(String roleName) {
        String normalizedRole = normalizeRole(roleName);
        return "ADMIN".equals(normalizedRole) || "MANAGER".equals(normalizedRole);
    }
}
