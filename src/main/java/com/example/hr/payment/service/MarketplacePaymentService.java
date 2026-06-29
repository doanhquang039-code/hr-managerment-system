package com.example.hr.payment.service;

import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.models.User;
import com.example.hr.payment.dto.CartDto;
import com.example.hr.payment.dto.CartItemDto;
import com.example.hr.rabbitmq.producer.EmailQueueProducer;
import com.example.hr.sales.entity.SalesCustomer;
import com.example.hr.sales.entity.SalesOrder;
import com.example.hr.sales.entity.SalesOrderItem;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.repository.SalesCustomerRepository;
import com.example.hr.sales.repository.SalesOrderRepository;
import com.example.hr.sales.repository.SalesProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service xử lý thanh toán Marketplace (giỏ hàng nội bộ HRMS).
 * Tách biệt hoàn toàn khỏi PaymentService (dùng cho lương/payroll).
 *
 * Chức năng:
 *  - Tạo đơn hàng từ giỏ hàng session
 *  - Sinh URL VietQR tự động cho sản phẩm Banking
 *  - Validate sản phẩm và tồn kho
 */
@Service
@RequiredArgsConstructor
public class MarketplacePaymentService {

    private final SalesProductRepository productRepository;
    private final SalesCustomerRepository customerRepository;
    private final SalesOrderRepository orderRepository;
    private final HREventProducer hrEventProducer;
    private final EmailQueueProducer emailQueueProducer;

    /**
     * Tạo CartItemDto từ SalesProduct để thêm vào giỏ hàng.
     */
    public CartItemDto buildCartItem(SalesProduct product, int quantity) {
        return new CartItemDto(
                product.getId(),
                product.getName(),
                product.getSeller() != null ? product.getSeller().getFullName() : "N/A",
                product.getPrice(),
                quantity,
                product.getImageUrl(),
                product.getQrImageUrl(),
                product.getPaymentProvider(),
                product.getPaymentNote()
        );
    }

    /**
     * Sinh VietQR URL tự động từ thông tin sản phẩm.
     * Chỉ áp dụng khi paymentProvider = BANKING và chưa có qrImageUrl tùy chỉnh.
     *
     * Format: https://img.vietqr.io/image/{bank}-{account}-compact.png?amount=...&addInfo=...
     */
    public String generateVietQrUrl(SalesProduct product) {
        if (product == null) return null;
        if (product.getQrImageUrl() != null && !product.getQrImageUrl().isBlank()) {
            return null; // Đã có QR tùy chỉnh, không cần VietQR
        }
        if (!"BANKING".equals(product.getPaymentProvider())) {
            return null; // VietQR chỉ cho Banking
        }
        String addInfo = product.getPaymentNote() != null && !product.getPaymentNote().isBlank()
                ? product.getPaymentNote()
                : "Thanh toan " + product.getName();
        long amount = product.getPrice() != null ? product.getPrice().longValue() : 0;
        String encoded = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);
        // Dùng MB Bank làm default — người bán có thể cấu hình sau
        return "https://img.vietqr.io/image/MB-0349123456-compact.png"
                + "?amount=" + amount
                + "&addInfo=" + encoded
                + "&accountName=HRMS+Marketplace";
    }

    /**
     * Tạo đơn hàng từ giỏ hàng session.
     * Validate sản phẩm đã duyệt và tồn kho đủ.
     */
    @Transactional
    public SalesOrder checkoutFromCart(CartDto cart, Integer customerId,
                                       String paymentMethod, String buyerNote, User buyer) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }
        SalesCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));

        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderCode("SO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        order.setStatus("PENDING");
        order.setNotes(buyerNote);
        order.setPaymentMethod(paymentMethod == null ? "COD" : paymentMethod);
        order.setBuyerNote(buyerNote);
        order.setCreatedBy(buyer);

        BigDecimal total = BigDecimal.ZERO;
        java.util.Set<User> sellers = new java.util.HashSet<>();

        for (CartItemDto ci : cart.getItems()) {
            SalesProduct product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + ci.getProductId()));

            if (!"APPROVED".equals(product.getApprovalStatus()) || !Boolean.TRUE.equals(product.getActive())) {
                throw new IllegalArgumentException("Sản phẩm chưa được duyệt: " + product.getName());
            }
            int qty = ci.getQuantity() == null || ci.getQuantity() < 1 ? 1 : ci.getQuantity();
            if (product.getStockQuantity() < qty) {
                throw new IllegalArgumentException("Tồn kho không đủ cho sản phẩm: " + product.getName());
            }

            SalesOrderItem item = new SalesOrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(qty)));
            order.getItems().add(item);
            total = total.add(item.getLineTotal());

            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);

            if (product.getSeller() != null) {
                sellers.add(product.getSeller());
            }
        }
        order.setTotalAmount(total);
        SalesOrder savedOrder = orderRepository.save(order);

        // Gửi thông báo cho Buyer
        if (buyer != null) {
            NotificationEvent buyerEvent = new NotificationEvent(
                    "IN_APP",
                    java.util.List.of(buyer.getId()),
                    "Đặt hàng thành công",
                    "Đơn hàng " + savedOrder.getOrderCode() + " đã được đặt thành công. Phương thức: " + savedOrder.getPaymentMethod(),
                    "MEDIUM",
                    "MARKETPLACE",
                    savedOrder.getId(),
                    "SalesOrder",
                    LocalDateTime.now()
            );
            hrEventProducer.publishNotificationEvent(buyerEvent);

            // Gửi email xác nhận đặt hàng cho Buyer qua RabbitMQ
            if (buyer.getEmail() != null && !buyer.getEmail().isBlank()) {
                java.util.Map<String, Object> emailTask = new java.util.HashMap<>();
                emailTask.put("to", buyer.getEmail());
                emailTask.put("subject", "Đặt hàng thành công - Đơn hàng " + savedOrder.getOrderCode());
                emailTask.put("body", "Chúc mừng! Đơn hàng " + savedOrder.getOrderCode() + 
                        " của bạn trên HRMS Marketplace đã đặt thành công.\n" +
                        "Tổng tiền: " + savedOrder.getTotalAmount() + " VNĐ.\n" +
                        "Phương thức thanh toán: " + savedOrder.getPaymentMethod() + ".");
                emailQueueProducer.sendEmailTask(emailTask);
            }
        }

        // Gửi thông báo cho từng Seller
        for (User seller : sellers) {
            NotificationEvent sellerEvent = new NotificationEvent(
                    "IN_APP",
                    java.util.List.of(seller.getId()),
                    "Đơn hàng mới",
                    "Bạn nhận được đơn hàng mới " + savedOrder.getOrderCode() + " cho sản phẩm trên Marketplace.",
                    "HIGH",
                    "MARKETPLACE",
                    savedOrder.getId(),
                    "SalesOrder",
                    LocalDateTime.now()
            );
            hrEventProducer.publishNotificationEvent(sellerEvent);

            // Gửi email thông báo đơn hàng mới cho Seller qua RabbitMQ
            if (seller.getEmail() != null && !seller.getEmail().isBlank()) {
                java.util.Map<String, Object> emailTask = new java.util.HashMap<>();
                emailTask.put("to", seller.getEmail());
                emailTask.put("subject", "Đơn hàng Marketplace mới - " + savedOrder.getOrderCode());
                emailTask.put("body", "Bạn nhận được một đơn hàng mới " + savedOrder.getOrderCode() + 
                        " cho sản phẩm đăng bán của bạn.\n" +
                        "Vui lòng truy cập hệ thống để kiểm tra và xử lý đơn hàng.");
                emailQueueProducer.sendEmailTask(emailTask);
            }
        }

        return savedOrder;
    }

    /**
     * Lấy danh sách đơn hàng marketplace (tất cả).
     */
    public java.util.List<SalesOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
}
