package com.example.hr.payment.dto;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * DTO đại diện một sản phẩm trong giỏ hàng marketplace.
 * Được lưu trong HTTP Session.
 */
public class CartItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer productId;
    private String productName;
    private String sellerName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String imageUrl;

    // Payment / QR info
    private String qrImageUrl;
    private String paymentProvider; // BANKING, MOMO, ZALOPAY
    private String paymentNote;     // Nội dung chuyển tiền mẫu

    public CartItemDto() {}

    public CartItemDto(Integer productId, String productName, String sellerName,
                       BigDecimal unitPrice, Integer quantity,
                       String imageUrl, String qrImageUrl,
                       String paymentProvider, String paymentNote) {
        this.productId = productId;
        this.productName = productName;
        this.sellerName = sellerName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.qrImageUrl = qrImageUrl;
        this.paymentProvider = paymentProvider;
        this.paymentNote = paymentNote;
    }

    /** Tổng tiền của dòng này */
    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ==================== Getters & Setters ====================
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getQrImageUrl() { return qrImageUrl; }
    public void setQrImageUrl(String qrImageUrl) { this.qrImageUrl = qrImageUrl; }

    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }

    public String getPaymentNote() { return paymentNote; }
    public void setPaymentNote(String paymentNote) { this.paymentNote = paymentNote; }
}
