package com.example.orchidservice.service.imp;

import com.example.orchidservice.dto.OrderDTO;
import com.example.orchidservice.dto.OrderDetailDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IOrderService {
    List<OrderDTO> getAllOrders();
    Optional<OrderDTO> getOrderById(Integer id);
    OrderDTO saveOrder(OrderDTO orderDTO);
    OrderDTO updateOrder(Integer id, OrderDTO orderDTO);
    void deleteOrder(Integer id);
    List<OrderDTO> getOrdersByAccount(Integer accountId);
    List<OrderDTO> getOrdersByStatus(String status);
    List<OrderDTO> getOrdersByDateRange(LocalDate startDate, LocalDate endDate);
    OrderDTO updateOrderStatus(Integer id, String status);
    Double calculateOrderTotal(Integer orderId);
    // Get order details for a specific order
    List<OrderDetailDTO> getOrderDetailsByOrderId(Integer orderId);
    // Get a specific order detail by ID
    Optional<OrderDetailDTO> getOrderDetailById(Integer orderDetailId);
    OrderDTO cancelOrder(Integer orderId);
}
