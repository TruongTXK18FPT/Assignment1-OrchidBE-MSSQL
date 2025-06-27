package com.example.orchidservice.controller;

import com.example.orchidservice.dto.OrderDTO;
import com.example.orchidservice.dto.CreateOrderRequest;
import com.example.orchidservice.dto.CartItemDTO;
import com.example.orchidservice.dto.OrderDetailDTO;
import com.example.orchidservice.pojo.Account;
import com.example.orchidservice.service.imp.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(
            @RequestBody CreateOrderRequest createOrderRequest,
            @AuthenticationPrincipal Account currentUser) {
        try {
            if (currentUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            // Create OrderDTO from the request
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setAccountId(currentUser.getAccountId());
            orderDTO.setOrderDate(LocalDate.now());
            orderDTO.setOrderStatus("pending");

            // Convert cart items to order details
            List<OrderDetailDTO> orderDetails = createOrderRequest.getItems().stream()
                    .map(cartItem -> {
                        OrderDetailDTO detail = new OrderDetailDTO();
                        detail.setOrchidId(cartItem.getOrchidId());
                        detail.setQuantity(cartItem.getQuantity());
                        detail.setPrice(cartItem.getPrice());
                        return detail;
                    })
                    .collect(Collectors.toList());

            orderDTO.setOrderDetails(orderDetails);

            // Save the order
            OrderDTO savedOrder = orderService.saveOrder(orderDTO);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByAccount(@PathVariable Integer accountId) {
        try {
            List<OrderDTO> orders = orderService.getOrdersByAccount(accountId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<OrderDTO> orders = orderService.getOrdersByStatus(status);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<OrderDTO>> getOrdersByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<OrderDTO> orders = orderService.getOrdersByDateRange(startDate, endDate);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String orderStatus) {
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(id, orderStatus);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<Double> calculateOrderTotal(@PathVariable Integer id) {
        try {
            Double total = orderService.calculateOrderTotal(id);
            return new ResponseEntity<>(total, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{orderId}/details")
    public ResponseEntity<List<OrderDetailDTO>> getOrderDetails(@PathVariable Integer orderId) {
        try {
            List<OrderDetailDTO> orderDetails = orderService.getOrderDetailsByOrderId(orderId);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/details/{orderDetailId}")
    public ResponseEntity<OrderDetailDTO> getOrderDetailById(@PathVariable Integer orderDetailId) {
        try {
            Optional<OrderDetailDTO> orderDetail = orderService.getOrderDetailById(orderDetailId);
            return orderDetail.map(detail -> new ResponseEntity<>(detail, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Integer id) {
        try {
            OrderDTO cancelledOrder = orderService.cancelOrder(id);
            return new ResponseEntity<>(cancelledOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}