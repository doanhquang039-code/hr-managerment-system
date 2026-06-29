package com.example.hr.sales.service;

import com.example.hr.engagement.service.EmployeeEngagementService;
import com.example.hr.enums.NotificationType;
import com.example.hr.models.User;
import com.example.hr.payment.dto.CartDto;
import com.example.hr.payment.dto.CartItemDto;
import com.example.hr.sales.entity.OrderChat;
import com.example.hr.sales.entity.ProductReview;
import com.example.hr.sales.entity.SalesCustomer;
import com.example.hr.sales.entity.SalesOrder;
import com.example.hr.sales.entity.SalesOrderItem;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.repository.OrderChatRepository;
import com.example.hr.sales.repository.ProductReviewRepository;
import com.example.hr.sales.repository.SalesCustomerRepository;
import com.example.hr.sales.repository.SalesOrderRepository;
import com.example.hr.sales.repository.SalesProductRepository;
import com.example.hr.elasticsearch.document.SalesProductDocument;
import com.example.hr.elasticsearch.repository.SalesProductSearchRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesService {

    private final SalesProductRepository productRepository;
    private final SalesCustomerRepository customerRepository;
    private final SalesOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmployeeEngagementService employeeEngagementService;
    private final ProductReviewRepository productReviewRepository;
    private final OrderChatRepository orderChatRepository;
    private final ObjectProvider<SalesProductSearchRepository> productSearchRepositoryProvider;
    private final NotificationService notificationService;

    @Cacheable(value = "marketplaceProducts")
    public List<SalesProduct> getProducts() {
        return productRepository.findByActiveTrueAndApprovalStatusOrderByCreatedAtDesc("APPROVED");
    }

    public List<SalesProduct> getProductsForApprovalView() {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<SalesProduct> getMyProducts(User seller) {
        return seller == null ? List.of() : productRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    public List<SalesProduct> getPendingProducts() {
        return productRepository.findByActiveTrueAndApprovalStatusOrderByCreatedAtDesc("PENDING");
    }

    public List<SalesCustomer> getCustomers() {
        return customerRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<SalesOrder> getOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SalesOrder> getPendingOrders() {
        return orderRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public SalesProduct getProduct(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay san pham"));
    }

    public boolean canManageProduct(SalesProduct product, User user) {
        if (product == null || user == null) return false;
        String roleName = normalizeRole(user.getEffectiveRoleName());
        return "ADMIN".equals(roleName)
                || "MANAGER".equals(roleName)
                || (product.getSeller() != null && product.getSeller().getId().equals(user.getId()));
    }

    public SalesCustomer getCustomer(Integer id) {
        return customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay khach hang"));
    }

    @Transactional
    @CacheEvict(value = "marketplaceProducts", allEntries = true)
    public SalesProduct saveProduct(SalesProduct product, User seller) {
        if (product.getPrice() == null) product.setPrice(BigDecimal.ZERO);
        if (product.getStockQuantity() == null) product.setStockQuantity(0);
        if (product.getActive() == null) product.setActive(true);
        if (product.getPaymentProvider() == null || product.getPaymentProvider().isBlank())
            product.setPaymentProvider("BANKING");
        product.setSeller(seller);
        if (seller != null) {
            String roleName = normalizeRole(seller.getEffectiveRoleName());
            if ("MANAGER".equals(roleName) || "ADMIN".equals(roleName)) {
                product.setApprovalStatus("APPROVED");
                product.setApprovedBy(seller);
                product.setApprovedAt(LocalDateTime.now());
            } else {
                product.setApprovalStatus("PENDING");
            }
        } else {
            product.setApprovalStatus("PENDING");
        }
        SalesProduct saved = productRepository.save(product);
        syncToElasticsearch(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = "marketplaceProducts", allEntries = true)
    public SalesProduct updateProduct(Integer productId, SalesProduct updated, User user) {
        SalesProduct product = getProduct(productId);
        if (!canManageProduct(product, user)) throw new IllegalArgumentException("Ban khong co quyen sua san pham nay");
        product.setName(updated.getName());
        product.setSku(updated.getSku());
        product.setDescription(updated.getDescription());
        product.setImageUrl(updated.getImageUrl());
        product.setPrice(updated.getPrice() == null ? BigDecimal.ZERO : updated.getPrice());
        product.setStockQuantity(updated.getStockQuantity() == null ? 0 : updated.getStockQuantity());
        product.setQrImageUrl(updated.getQrImageUrl());
        product.setPaymentProvider(updated.getPaymentProvider() == null ? "BANKING" : updated.getPaymentProvider());
        product.setPaymentNote(updated.getPaymentNote());
        String roleName = normalizeRole(user.getEffectiveRoleName());
        if (!"ADMIN".equals(roleName) && !"MANAGER".equals(roleName)) {
            product.setApprovalStatus("PENDING");
            product.setApprovedBy(null);
            product.setApprovedAt(null);
        }
        SalesProduct saved = productRepository.save(product);
        syncToElasticsearch(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = "marketplaceProducts", allEntries = true)
    public void deactivateProduct(Integer productId, User user) {
        SalesProduct product = getProduct(productId);
        if (!canManageProduct(product, user)) throw new IllegalArgumentException("Ban khong co quyen xoa san pham nay");
        product.setActive(false);
        productRepository.save(product);
        syncToElasticsearch(product);
    }

    @Transactional
    @CacheEvict(value = "marketplaceProducts", allEntries = true)
    public SalesProduct approveProduct(Integer productId, User approver) {
        SalesProduct product = getProduct(productId);
        product.setApprovalStatus("APPROVED");
        product.setApprovedBy(approver);
        product.setApprovedAt(LocalDateTime.now());
        product.setActive(true);
        SalesProduct saved = productRepository.save(product);
        syncToElasticsearch(saved);
        notifySellerProductReviewed(saved, true);
        return saved;
    }

    @Transactional
    @CacheEvict(value = "marketplaceProducts", allEntries = true)
    public SalesProduct rejectProduct(Integer productId, User approver) {
        SalesProduct product = getProduct(productId);
        product.setApprovalStatus("REJECTED");
        product.setApprovedBy(approver);
        product.setApprovedAt(LocalDateTime.now());
        SalesProduct saved = productRepository.save(product);
        syncToElasticsearch(saved);
        notifySellerProductReviewed(saved, false);
        return saved;
    }

    @Transactional
    public SalesCustomer saveCustomer(SalesCustomer customer) {
        if (customer.getActive() == null) customer.setActive(true);
        return customerRepository.save(customer);
    }

    /** Tạo đơn hàng đơn lẻ (từ Manager dashboard) */
    @Transactional
    public SalesOrder createOrder(Integer customerId, Integer productId, Integer quantity, String notes, User creator) {
        SalesCustomer customer = getCustomer(customerId);
        SalesProduct product = getProduct(productId);
        validateProductForOrder(product);
        int qty = quantity == null || quantity < 1 ? 1 : quantity;
        validateStock(product, qty);
        SalesOrder order = buildOrder(customer, notes, "COD");
        order.setCreatedBy(creator);
        SalesOrderItem item = buildItem(order, product, qty);
        order.getItems().add(item);
        order.setTotalAmount(item.getLineTotal());
        product.setStockQuantity(product.getStockQuantity() - qty);
        productRepository.save(product);
        return orderRepository.save(order);
    }

    /** Tạo đơn hàng từ giỏ hàng session */
    @Transactional
    public SalesOrder createOrderFromCart(CartDto cart, Integer customerId, String paymentMethod, String buyerNote) {
        if (cart == null || cart.isEmpty()) throw new IllegalArgumentException("Gio hang trong");
        SalesCustomer customer = getCustomer(customerId);
        SalesOrder order = buildOrder(customer, buyerNote, paymentMethod);
        order.setBuyerNote(buyerNote);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDto ci : cart.getItems()) {
            SalesProduct product = getProduct(ci.getProductId());
            validateProductForOrder(product);
            int qty = ci.getQuantity() == null || ci.getQuantity() < 1 ? 1 : ci.getQuantity();
            validateStock(product, qty);
            SalesOrderItem item = buildItem(order, product, qty);
            order.getItems().add(item);
            total = total.add(item.getLineTotal());
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);
        }
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional
    public SalesOrder updateOrderStatus(Integer orderId, String status) {
        SalesOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang"));
        String oldStatus = order.getStatus();
        order.setStatus(status == null || status.isBlank() ? "PENDING" : status);
        SalesOrder savedOrder = orderRepository.save(order);
        if (!savedOrder.getStatus().equals(oldStatus)) {
            if ("APPROVED".equals(savedOrder.getStatus())) {
                notifyOrderReviewed(savedOrder, true);
            } else if ("CANCELLED".equals(savedOrder.getStatus()) || "REJECTED".equals(savedOrder.getStatus())) {
                notifyOrderReviewed(savedOrder, false);
            }
        }

        // Tích điểm Loyalty Points khi đơn hàng chuyển sang COMPLETED
        if ("COMPLETED".equals(status) && !"COMPLETED".equals(oldStatus)) {
            try {
                if (order.getCustomer() != null && order.getCustomer().getEmail() != null) {
                    User buyerUser = userRepository.findByEmail(order.getCustomer().getEmail()).orElse(null);
                    if (buyerUser != null) {
                        // 1% giá trị đơn hàng (quy đổi 1 điểm = 100đ, hoặc đơn giản 1% tổng tiền thành điểm)
                        int points = order.getTotalAmount().multiply(new BigDecimal("0.01")).intValue();
                        if (points < 1) points = 1;

                        employeeEngagementService.giveRecognition(
                                buyerUser,
                                null, // Hệ thống tự động tặng
                                "LOYALTY",
                                "Tích lũy Loyalty Points",
                                "Tích lũy điểm thưởng mua sắm từ đơn hàng hoàn tất " + order.getOrderCode(),
                                points,
                                false
                        );
                        log.info("Đã tích lũy {} điểm thưởng cho user {} từ đơn hàng {}", points, buyerUser.getUsername(), order.getOrderCode());
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi khi cộng điểm Loyalty cho đơn hàng: {}", order.getOrderCode(), e);
            }
        }
        return savedOrder;
    }

    @Transactional
    public SalesOrder approveOrder(Integer orderId) {
        return updateOrderStatus(orderId, "APPROVED");
    }

    @Transactional
    public SalesOrder rejectOrder(Integer orderId) {
        return updateOrderStatus(orderId, "CANCELLED");
    }

    public SalesOrder getOrder(Integer id) {
        return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang"));
    }

    @Transactional
    public SalesOrder saveOrder(SalesOrder order) {
        return orderRepository.save(order);
    }

    public List<OrderChat> getOrderChats(Integer orderId) {
        return orderChatRepository.findByOrderIdOrderByTimestampAsc(orderId);
    }

    @Transactional
    public OrderChat saveOrderChat(Integer orderId, User sender, String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Tin nhan khong duoc de trong");
        }
        SalesOrder order = getOrder(orderId);
        OrderChat chat = new OrderChat();
        chat.setOrder(order);
        chat.setSender(sender);
        chat.setMessage(message);
        return orderChatRepository.save(chat);
    }

    public List<ProductReview> getProductReviews(Integer productId) {
        return productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Double getAverageRating(Integer productId) {
        Double avg = productReviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Transactional
    public ProductReview addProductReview(Integer productId, User buyer, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating phai tu 1 den 5 sao");
        }
        SalesProduct product = getProduct(productId);
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setRating(rating);
        review.setComment(comment);
        return productReviewRepository.save(review);
    }

    public List<SalesProduct> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getProducts();
        }
        var searchRepo = productSearchRepositoryProvider.getIfAvailable();
        if (searchRepo != null) {
            try {
                List<SalesProductDocument> docs = searchRepo.findByNameContainingOrDescriptionContaining(query, query);
                List<Integer> ids = docs.stream().map(SalesProductDocument::getId).toList();
                if (!ids.isEmpty()) {
                    return productRepository.findAllById(ids);
                }
            } catch (Exception e) {
                log.error("Elasticsearch query failed, falling back to DB: {}", e.getMessage());
            }
        }
        // Fallback: Tìm kiếm trong CSDL
        return productRepository.searchActiveProducts(query);
    }

    private void syncToElasticsearch(SalesProduct product) {
        productSearchRepositoryProvider.ifAvailable(repo -> {
            try {
                if (product.getActive() == null || !product.getActive() || !"APPROVED".equals(product.getApprovalStatus())) {
                    repo.deleteById(product.getId());
                    log.info("Deleted product ID {} from Elasticsearch", product.getId());
                } else {
                    SalesProductDocument doc = new SalesProductDocument(
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            product.getDescription(),
                            product.getPrice() != null ? product.getPrice().doubleValue() : 0.0,
                            product.getStockQuantity() != null ? product.getStockQuantity() : 0,
                            product.getImageUrl(),
                            product.getSeller() != null ? product.getSeller().getFullName() : "N/A",
                            product.getApprovalStatus(),
                            product.getActive()
                    );
                    repo.save(doc);
                    log.info("Synchronized product ID {} to Elasticsearch", product.getId());
                }
            } catch (Exception e) {
                log.error("Failed to sync product ID {} to Elasticsearch: {}", product.getId(), e.getMessage());
            }
        });
    }

    // ==================== Private Helpers ====================

    private void validateProductForOrder(SalesProduct p) {
        if (!"APPROVED".equals(p.getApprovalStatus()) || !Boolean.TRUE.equals(p.getActive()))
            throw new IllegalArgumentException("San pham chua duoc duyet: " + p.getName());
    }

    private void validateStock(SalesProduct p, int qty) {
        int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
        if (stock < qty)
            throw new IllegalArgumentException("Ton kho khong du cho san pham: " + p.getName());
    }

    private SalesOrder buildOrder(SalesCustomer customer, String notes, String paymentMethod) {
        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderCode("SO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        order.setStatus("PENDING");
        order.setNotes(notes);
        order.setPaymentMethod(paymentMethod == null ? "COD" : paymentMethod);
        return order;
    }

    private SalesOrderItem buildItem(SalesOrder order, SalesProduct product, int qty) {
        SalesOrderItem item = new SalesOrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(qty);
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        return item;
    }

    private String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return "USER";
        return roleName.trim().replaceFirst("^ROLE_", "").toUpperCase();
    }

    private void notifySellerProductReviewed(SalesProduct product, boolean approved) {
        if (product == null || product.getSeller() == null) return;
        String productName = product.getName() == null || product.getName().isBlank()
                ? "san pham cua ban"
                : product.getName();
        String message = approved
                ? "San pham \"" + productName + "\" cua ban da duoc Manager duyet."
                : "San pham \"" + productName + "\" cua ban da bi tu choi.";
        NotificationType type = approved ? NotificationType.SUCCESS : NotificationType.WARNING;
        notificationService.createNotification(product.getSeller(), message, type, "/sales/products/" + product.getId());
    }

    private void notifyOrderReviewed(SalesOrder order, boolean approved) {
        if (order == null) return;
        String orderCode = order.getOrderCode() == null ? "don hang" : order.getOrderCode();
        String statusText = approved ? "da duoc Manager duyet" : "da bi Manager tu choi";
        NotificationType type = approved ? NotificationType.SUCCESS : NotificationType.WARNING;
        String link = "/sales/orders/" + order.getId();

        java.util.Set<Integer> notifiedUserIds = new java.util.HashSet<>();
        User buyer = order.getCreatedBy();
        if (buyer == null && order.getCustomer() != null && order.getCustomer().getEmail() != null) {
            buyer = userRepository.findByEmail(order.getCustomer().getEmail()).orElse(null);
        }
        if (buyer != null && buyer.getId() != null) {
            notificationService.createNotification(
                    buyer,
                    "Don hang " + orderCode + " cua ban " + statusText + ".",
                    type,
                    link
            );
            notifiedUserIds.add(buyer.getId());
        }

        if (order.getItems() == null) return;
        for (SalesOrderItem item : order.getItems()) {
            if (item == null || item.getProduct() == null || item.getProduct().getSeller() == null) continue;
            User seller = item.getProduct().getSeller();
            if (seller.getId() == null || notifiedUserIds.contains(seller.getId())) continue;
            notificationService.createNotification(
                    seller,
                    "Don hang " + orderCode + " co san pham cua ban " + statusText + ".",
                    type,
                    link
            );
            notifiedUserIds.add(seller.getId());
        }
    }
}
