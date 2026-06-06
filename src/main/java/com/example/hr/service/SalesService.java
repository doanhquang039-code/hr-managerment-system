package com.example.hr.service;

import com.example.hr.models.SalesCustomer;
import com.example.hr.models.SalesOrder;
import com.example.hr.models.SalesOrderItem;
import com.example.hr.models.SalesProduct;
import com.example.hr.models.User;
import com.example.hr.repository.SalesCustomerRepository;
import com.example.hr.repository.SalesOrderRepository;
import com.example.hr.repository.SalesProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesProductRepository productRepository;
    private final SalesCustomerRepository customerRepository;
    private final SalesOrderRepository orderRepository;

    public List<SalesProduct> getProducts() {
        return productRepository.findByActiveTrueAndApprovalStatusOrderByCreatedAtDesc("APPROVED");
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

    public SalesProduct getProduct(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay san pham"));
    }

    public SalesCustomer getCustomer(Integer id) {
        return customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay khach hang"));
    }

    @Transactional
    public SalesProduct saveProduct(SalesProduct product, User seller) {
        if (product.getPrice() == null) {
            product.setPrice(BigDecimal.ZERO);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getActive() == null) {
            product.setActive(true);
        }
        product.setSeller(seller);
        if (seller != null && (seller.getRole() == com.example.hr.enums.Role.MANAGER || seller.getRole() == com.example.hr.enums.Role.ADMIN)) {
            product.setApprovalStatus("APPROVED");
            product.setApprovedBy(seller);
            product.setApprovedAt(LocalDateTime.now());
        } else {
            product.setApprovalStatus("PENDING");
        }
        return productRepository.save(product);
    }

    @Transactional
    public SalesProduct approveProduct(Integer productId, User approver) {
        SalesProduct product = getProduct(productId);
        product.setApprovalStatus("APPROVED");
        product.setApprovedBy(approver);
        product.setApprovedAt(LocalDateTime.now());
        product.setActive(true);
        return productRepository.save(product);
    }

    @Transactional
    public SalesProduct rejectProduct(Integer productId, User approver) {
        SalesProduct product = getProduct(productId);
        product.setApprovalStatus("REJECTED");
        product.setApprovedBy(approver);
        product.setApprovedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    public SalesCustomer saveCustomer(SalesCustomer customer) {
        if (customer.getActive() == null) {
            customer.setActive(true);
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public SalesOrder createOrder(Integer customerId, Integer productId, Integer quantity, String notes) {
        SalesCustomer customer = getCustomer(customerId);
        SalesProduct product = getProduct(productId);
        if (!"APPROVED".equals(product.getApprovalStatus()) || !Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("San pham chua duoc duyet");
        }
        int orderedQuantity = quantity == null || quantity < 1 ? 1 : quantity;
        if (product.getStockQuantity() < orderedQuantity) {
            throw new IllegalArgumentException("Ton kho khong du cho san pham " + product.getName());
        }

        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderCode("SO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        order.setStatus("PENDING");
        order.setNotes(notes);

        SalesOrderItem item = new SalesOrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(orderedQuantity);
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(orderedQuantity)));
        order.getItems().add(item);
        order.setTotalAmount(item.getLineTotal());

        product.setStockQuantity(product.getStockQuantity() - orderedQuantity);
        productRepository.save(product);
        return orderRepository.save(order);
    }

    @Transactional
    public SalesOrder updateOrderStatus(Integer orderId, String status) {
        SalesOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang"));
        order.setStatus(status == null || status.isBlank() ? "PENDING" : status);
        return orderRepository.save(order);
    }
}
