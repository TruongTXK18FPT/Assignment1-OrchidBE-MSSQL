package com.example.orchidservice.controller;

import com.example.orchidservice.dto.AccountDTO;
import com.example.orchidservice.dto.ApiResponse;
import com.example.orchidservice.dto.LoginRequestDTO;
import com.example.orchidservice.dto.LoginResponseDTO;
import com.example.orchidservice.dto.RegisterRequestDTO;
import com.example.orchidservice.dto.RegisterResponseDTO;
import com.example.orchidservice.dto.UpdateProfileRequestDTO;
import com.example.orchidservice.dto.ForgotPasswordRequestDTO;
import com.example.orchidservice.dto.ResetPasswordRequestDTO;
import com.example.orchidservice.pojo.Account;
import com.example.orchidservice.service.imp.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final IAccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response = accountService.register(request);
        return ResponseEntity.ok(ApiResponse.<RegisterResponseDTO>builder()
            .code(1000)
            .message("Registration successful")
            .result(response)
            .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = accountService.login(request);
        return ResponseEntity.ok(ApiResponse.<LoginResponseDTO>builder()
            .code(1000)
            .message("Login successful")
            .result(response)
            .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AccountDTO>> getCurrentUserProfile(@AuthenticationPrincipal Account currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AccountDTO>builder()
                            .code(1001)
                            .message("Unauthorized access")
                            .build());
        }

        AccountDTO accountDTO = AccountDTO.builder()
                .accountId(currentUser.getAccountId())
                .accountName(currentUser.getAccountName())
                .email(currentUser.getEmail())
                .roleId(currentUser.getRole().getRoleId())
                .roleName(currentUser.getRole().getRoleName())
                .build();

        return ResponseEntity.ok(ApiResponse.<AccountDTO>builder()
                .code(1000)
                .message("Profile retrieved successfully")
                .result(accountDTO)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        accountService.logout(token);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(1000)
                .message("Logged out successfully")
                .build());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AccountDTO>> updateProfile(
            @AuthenticationPrincipal Account currentUser,
            @Valid @RequestBody UpdateProfileRequestDTO request) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AccountDTO>builder()
                            .code(1001)
                            .message("Unauthorized access")
                            .build());
        }

        AccountDTO updatedProfile = accountService.updateProfile(currentUser, request);

        return ResponseEntity.ok(ApiResponse.<AccountDTO>builder()
                .code(1000)
                .message("Profile updated successfully")
                .result(updatedProfile)
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        try {
            Optional<Account> accountOpt = accountService.getAccountByEmail(request.getEmail());
            if (accountOpt.isPresent()) {
                // Generate base64 token from email
                String token = java.util.Base64.getEncoder().encodeToString(request.getEmail().getBytes());
                System.out.println("Reset token for " + request.getEmail() + ": " + token);
                System.out.println("Reset URL: http://localhost:5173/forgot-password?token=" + token);

                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .code(1000)
                        .message("Password reset link generated. Check console for token.")
                        .result(token) // Return token for testing
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<String>builder()
                                .code(1004)
                                .message("Email not found")
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .code(1005)
                            .message("Failed to process request")
                            .build());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        accountService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(1000)
                .message("Password reset successfully")
                .build());
    }
}
