package com.example.hr.payment.dto;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Giỏ hàng marketplace — lưu trữ trong HTTP Session.
 * Không cần persistence vào database.
 */
public class CartDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CartItemDto> items = new ArrayList<>();

    public CartDto() {}

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    /** Tổng số lượng tất cả item */
    public int getTotalItems() {
        return items.stream().mapToInt(i -> i.getQuantity() == null ? 0 : i.getQuantity()).sum();
    }

    /** Tổng tiền toàn bộ giỏ */
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Kiểm tra giỏ trống */
    public boolean isEmpty() { return items == null || items.isEmpty(); }

    /** Tìm item theo productId */
    public CartItemDto findByProductId(Integer productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst().orElse(null);
    }

    /** Thêm hoặc cộng dồn số lượng */
    public void addItem(CartItemDto newItem) {
        CartItemDto existing = findByProductId(newItem.getProductId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
        } else {
            items.add(newItem);
        }
    }

    /** Xóa item theo productId */
    public void removeItem(Integer productId) {
        items.removeIf(i -> i.getProductId().equals(productId));
    }

    /** Cập nhật số lượng — nếu qty <= 0 thì xóa */
    public void updateQuantity(Integer productId, int qty) {
        CartItemDto item = findByProductId(productId);
        if (item != null) {
            if (qty <= 0) {
                removeItem(productId);
            } else {
                item.setQuantity(qty);
            }
        }
    }
}
