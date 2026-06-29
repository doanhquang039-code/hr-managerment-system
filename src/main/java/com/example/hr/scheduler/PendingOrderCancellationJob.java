package com.example.hr.scheduler;

import com.example.hr.sales.entity.SalesOrder;
import com.example.hr.sales.entity.SalesOrderItem;
import com.example.hr.sales.entity.SalesProduct;
import com.example.hr.sales.repository.SalesOrderRepository;
import com.example.hr.sales.repository.SalesProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quartz Job tự động hủy các đơn hàng Marketplace trạng thái PENDING quá 24h
 * và hoàn lại số lượng tồn kho (stockQuantity) cho sản phẩm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingOrderCancellationJob implements Job {

    private final SalesOrderRepository orderRepository;
    private final SalesProductRepository productRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Bắt đầu Quartz Job quét và hủy đơn hàng quá hạn thanh toán...");
        try {
            List<SalesOrder> pendingOrders = orderRepository.findByStatus("PENDING");
            LocalDateTime limitTime = LocalDateTime.now().minusHours(24);
            int cancelledCount = 0;

            for (SalesOrder order : pendingOrders) {
                if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(limitTime)) {
                    log.info("Hủy đơn hàng quá hạn: {} (Tạo lúc: {})", order.getOrderCode(), order.getCreatedAt());
                    order.setStatus("CANCELLED");
                    order.setNotes(order.getNotes() != null 
                            ? order.getNotes() + "\n[System: Tự động hủy do quá 24h chưa thanh toán]"
                            : "[System: Tự động hủy do quá 24h chưa thanh toán]");
                    
                    // Hoàn lại số lượng tồn kho cho từng sản phẩm
                    for (SalesOrderItem item : order.getItems()) {
                        SalesProduct product = item.getProduct();
                        if (product != null) {
                            int restoredStock = product.getStockQuantity() + item.getQuantity();
                            log.info("Hoàn tồn kho sản phẩm ID {}: {} -> {}", product.getId(), product.getStockQuantity(), restoredStock);
                            product.setStockQuantity(restoredStock);
                            productRepository.save(product);
                        }
                    }
                    orderRepository.save(order);
                    cancelledCount++;
                }
            }
            log.info("Kết thúc Job quét đơn hàng. Đã hủy {} đơn hàng quá hạn.", cancelledCount);
        } catch (Exception e) {
            log.error("Lỗi khi chạy PendingOrderCancellationJob", e);
            throw new JobExecutionException(e);
        }
    }
}
