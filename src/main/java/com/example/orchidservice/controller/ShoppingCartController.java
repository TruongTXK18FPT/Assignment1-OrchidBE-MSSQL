package com.example.orchidservice.controller;

import com.example.orchidservice.dto.ShoppingCartDTO;
import com.example.orchidservice.service.imp.IShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    @Autowired
    private IShoppingCartService shoppingCartService;

    @GetMapping
    public ResponseEntity<ShoppingCartDTO> getCart(HttpSession session) {
        try {
            String sessionId = session.getId();
            ShoppingCartDTO cart = shoppingCartService.getCart(sessionId);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ShoppingCartDTO> addToCart(
            @RequestParam Integer orchidId,
            @RequestParam Integer quantity,
            HttpSession session) {
        try {
            if (quantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            String sessionId = session.getId();
            session.setMaxInactiveInterval(3600); // Set session timeout to 1 hour
            ShoppingCartDTO cart = shoppingCartService.addToCart(sessionId, orchidId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ShoppingCartDTO> updateCartItem(
            @RequestParam Integer orchidId,
            @RequestParam Integer quantity,
            HttpSession session) {
        String sessionId = session.getId();
        ShoppingCartDTO cart = shoppingCartService.updateCartItem(sessionId, orchidId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{orchidId}")
    public ResponseEntity<ShoppingCartDTO> removeFromCart(
            @PathVariable Integer orchidId,
            HttpSession session) {
        String sessionId = session.getId();
        ShoppingCartDTO cart = shoppingCartService.removeFromCart(sessionId, orchidId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(HttpSession session) {
        String sessionId = session.getId();
        shoppingCartService.clearCart(sessionId);
        return ResponseEntity.noContent().build();
    }
}