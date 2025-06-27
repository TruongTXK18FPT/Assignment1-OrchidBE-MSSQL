package com.example.orchidservice.controller;

import com.example.orchidservice.dto.AccountDTO;
import com.example.orchidservice.dto.CategoryDTO;
import com.example.orchidservice.dto.OrchidDTO;
import com.example.orchidservice.dto.OrderDTO;
import com.example.orchidservice.pojo.Account;
import com.example.orchidservice.pojo.Role;
import com.example.orchidservice.service.imp.IAccountService;
import com.example.orchidservice.service.imp.ICategoryService;
import com.example.orchidservice.service.imp.IOrchidService;
import com.example.orchidservice.service.imp.IOrderService;
import com.example.orchidservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class AdminController {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IOrchidService orchidService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IAccountService accountService;
    @Autowired
    private RoleRepository roleRepository;

    // Category CRUD Operations
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Integer id) {
        Optional<CategoryDTO> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO created = categoryService.saveCategory(categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updated = categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Orchid CRUD Operations
    @GetMapping("/orchids")
    public ResponseEntity<List<OrchidDTO>> getAllOrchids() {
        List<OrchidDTO> orchids = orchidService.getAllOrchids();
        return ResponseEntity.ok(orchids);
    }

    @GetMapping("/orchids/{id}")
    public ResponseEntity<OrchidDTO> getOrchidById(@PathVariable Integer id) {
        Optional<OrchidDTO> orchid = orchidService.getOrchidById(id);
        return orchid.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/orchids")
    public ResponseEntity<OrchidDTO> createOrchid(@RequestBody OrchidDTO orchidDTO) {
        try {
            OrchidDTO created = orchidService.saveOrchid(orchidDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/orchids/{id}")
    public ResponseEntity<OrchidDTO> updateOrchid(@PathVariable Integer id, @RequestBody OrchidDTO orchidDTO) {
        try {
            OrchidDTO updated = orchidService.updateOrchid(id, orchidDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/orchids/{id}")
    public ResponseEntity<Void> deleteOrchid(@PathVariable Integer id) {
        orchidService.deleteOrchid(id);
        return ResponseEntity.noContent().build();
    }

    // Order CRUD Operations
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        try {
            List<OrderDTO> orders = orderService.getAllOrders();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        try {
            Optional<OrderDTO> order = orderService.getOrderById(id);
            return order.map(o -> new ResponseEntity<>(o, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            OrderDTO savedOrder = orderService.saveOrder(orderDTO);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer id, @RequestBody OrderDTO orderDTO) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(id, orderDTO);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Account CRUD Operations
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            List<AccountDTO> accountDTOs = accounts.stream()
                    .map(account -> AccountDTO.builder()
                            .accountId(account.getAccountId())
                            .accountName(account.getAccountName())
                            .email(account.getEmail())
                            .roleId(account.getRole().getRoleId())
                            .roleName(account.getRole().getRoleName())
                            .build())
                    .toList();
            return new ResponseEntity<>(accountDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Integer id) {
        try {
            Optional<Account> accountOpt = accountService.getAccountById(id);
            return accountOpt.map(account -> {
                AccountDTO accountDTO = AccountDTO.builder()
                        .accountId(account.getAccountId())
                        .accountName(account.getAccountName())
                        .email(account.getEmail())
                        .roleId(account.getRole().getRoleId())
                        .roleName(account.getRole().getRoleName())
                        .build();
                return new ResponseEntity<>(accountDTO, HttpStatus.OK);
            }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountDTO accountDTO) {
        try {
            Account account = new Account();
            account.setAccountName(accountDTO.getAccountName());
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());
            Role role = roleRepository.findById(accountDTO.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            account.setRole(role);

            Account savedAccount = accountService.saveAccount(account);
            AccountDTO savedAccountDTO = AccountDTO.builder()
                    .accountId(savedAccount.getAccountId())
                    .accountName(savedAccount.getAccountName())
                    .email(savedAccount.getEmail())
                    .roleId(savedAccount.getRole().getRoleId())
                    .roleName(savedAccount.getRole().getRoleName())
                    .build();
            return new ResponseEntity<>(savedAccountDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Integer id, @RequestBody AccountDTO accountDTO) {
        try {
            Optional<Account> accountOpt = accountService.getAccountById(id);
            if (accountOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Account account = accountOpt.get();
            account.setAccountName(accountDTO.getAccountName());
            account.setEmail(accountDTO.getEmail());
            if (accountDTO.getPassword() != null && !accountDTO.getPassword().isEmpty()) {
                account.setPassword(accountDTO.getPassword());
            }
            Role role = roleRepository.findById(accountDTO.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            account.setRole(role);

            Account updatedAccount = accountService.saveAccount(account);
            AccountDTO updatedAccountDTO = AccountDTO.builder()
                    .accountId(updatedAccount.getAccountId())
                    .accountName(updatedAccount.getAccountName())
                    .email(updatedAccount.getEmail())
                    .roleId(updatedAccount.getRole().getRoleId())
                    .roleName(updatedAccount.getRole().getRoleName())
                    .build();
            return new ResponseEntity<>(updatedAccountDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer id) {
        try {
            accountService.deleteAccount(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}