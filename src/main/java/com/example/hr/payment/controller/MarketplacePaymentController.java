package com.example.hr.payment.controller;

import com.example.hr.models.User;
import com.example.hr.payment.dto.CartDto;
import com.example.hr.payment.dto.CartItemDto;
import com.example.hr.payment.service.MarketplacePaymentService;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.service.SalesService;
import com.example.hr.service.AuthUserHelper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý giỏ hàng và thanh toán Marketplace.
 * Tách biệt khỏi SalesController (quản lý sản phẩm) và PaymentController (lương/payroll).
 *
 * URL pattern: /sales/cart/** (giữ cùng URL để không ảnh hưởng frontend)
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping({"/sales/cart", "/admin/sales/cart", "/manager/sales/cart"})
public class MarketplacePaymentController {

    private final MarketplacePaymentService marketplacePaymentService;
    private final SalesService salesService;
    private final AuthUserHelper authUserHelper;

    private static final String CART_SESSION_KEY = "salesCart";

    // ==================== GIỎ HÀNG ====================

    /**
     * Xem giỏ hàng hiện tại.
     */
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("customers", salesService.getCustomers());
        return "sales/cart";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng (redirect từ product detail page).
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session, RedirectAttributes ra) {
        try {
            SalesProduct product = salesService.getProduct(productId);
            if (!"APPROVED".equals(product.getApprovalStatus())) {
                ra.addFlashAttribute("error", "Sản phẩm chưa được duyệt, không thể thêm vào giỏ");
                return "redirect:/sales/products/" + productId;
            }
            if (product.getStockQuantity() <= 0) {
                ra.addFlashAttribute("error", "Sản phẩm đã hết hàng");
                return "redirect:/sales/products/" + productId;
            }

            CartDto cart = getCart(session);
            CartItemDto item = marketplacePaymentService.buildCartItem(product, Math.max(1, quantity));
            cart.addItem(item);
            session.setAttribute(CART_SESSION_KEY, cart);

            ra.addFlashAttribute("success", "Đã thêm \"" + product.getName() + "\" vào giỏ hàng 🛒");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi thêm vào giỏ: " + e.getMessage());
        }
        return "redirect:/sales/products/" + productId;
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng.
     */
    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Integer productId,
                                 HttpSession session, RedirectAttributes ra) {
        CartDto cart = getCart(session);
        cart.removeItem(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
        ra.addFlashAttribute("success", "Đã xóa khỏi giỏ hàng");
        return "redirect:/sales/cart";
    }

    /**
     * Cập nhật số lượng item trong giỏ.
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Integer productId,
                                 @RequestParam Integer quantity,
                                 HttpSession session) {
        CartDto cart = getCart(session);
        cart.updateQuantity(productId, quantity);
        session.setAttribute(CART_SESSION_KEY, cart);
        return "redirect:/sales/cart";
    }

    /**
     * Xóa toàn bộ giỏ hàng.
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute(CART_SESSION_KEY);
        ra.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng");
        return "redirect:/sales/products";
    }

    // ==================== THANH TOÁN / CHECKOUT ====================

    /**
     * Đặt hàng từ giỏ hàng.
     * Tạo SalesOrder → ghi payment method → trừ tồn kho.
     */
    @PostMapping("/checkout")
    public String checkout(@RequestParam Integer customerId,
                           @RequestParam(defaultValue = "COD") String paymentMethod,
                           @RequestParam(required = false) String buyerNote,
                           Authentication auth,
                           HttpSession session, RedirectAttributes ra) {
        try {
            CartDto cart = getCart(session);
            if (cart.isEmpty()) {
                ra.addFlashAttribute("error", "Giỏ hàng trống, vui lòng thêm sản phẩm trước");
                return "redirect:/sales/cart";
            }
            User buyer = authUserHelper.getCurrentUser(auth);
            marketplacePaymentService.checkoutFromCart(cart, customerId, paymentMethod, buyerNote, buyer);
            session.removeAttribute(CART_SESSION_KEY); // Xóa giỏ sau khi đặt hàng thành công
            ra.addFlashAttribute("success", "🎉 Đặt hàng thành công! Vui lòng hoàn tất thanh toán.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể đặt hàng: " + e.getMessage());
            return "redirect:/sales/cart";
        }
        return "redirect:/sales/orders";
    }

    // ==================== Helper ====================

    private CartDto getCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
}
