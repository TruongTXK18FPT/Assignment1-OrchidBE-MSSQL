package com.example.orchidservice.service;

import com.example.orchidservice.dto.LoginRequestDTO;
import com.example.orchidservice.dto.LoginResponseDTO;
import com.example.orchidservice.dto.RegisterRequestDTO;
import com.example.orchidservice.dto.RegisterResponseDTO;
import com.example.orchidservice.dto.UpdateProfileRequestDTO;
import com.example.orchidservice.dto.ForgotPasswordRequestDTO;
import com.example.orchidservice.dto.ResetPasswordRequestDTO;
import com.example.orchidservice.dto.AccountDTO;
import com.example.orchidservice.pojo.Account;
import com.example.orchidservice.pojo.Role;
import com.example.orchidservice.repository.AccountRepository;
import com.example.orchidservice.repository.RoleRepository;
import com.example.orchidservice.service.imp.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getAccountById(Integer id) {
        return accountRepository.findById(id);
    }

    @Override
    @Transactional
    public Account saveAccount(Account account) {
        if (account.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Integer id) {
        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        accountRepository.deleteById(id);
    }

    @Override
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public List<Account> getAccountsByRoleId(Integer roleId) {
        return accountRepository.findByRoleRoleId(roleId);
    }

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        Role defaultRole = roleRepository.findById(3)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default user role not found"));

        Account newAccount = new Account();
        newAccount.setAccountName(request.getAccountName());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        newAccount.setRole(defaultRole);

        Account savedAccount = accountRepository.save(newAccount);

        return RegisterResponseDTO.builder()
                .accountId(savedAccount.getAccountId())
                .accountName(savedAccount.getAccountName())
                .email(savedAccount.getEmail())
                .roleId(savedAccount.getRole().getRoleId())
                .build();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or password");
        }

        String token = jwtService.generateToken(account);

        return LoginResponseDTO.builder()
                .accountId(account.getAccountId())
                .accountName(account.getAccountName())
                .email(account.getEmail())
                .roleId(account.getRole().getRoleId())
                .roleName(account.getRole().getRoleName())
                .success(true)
                .message("Login successful")
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null) {
            jwtService.invalidateToken(token);
        }
    }

    @Override
    @Transactional
    public AccountDTO updateProfile(Account currentUser, UpdateProfileRequestDTO request) {
        Account account = accountRepository.findById(currentUser.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        account.setAccountName(request.getAccountName());
        Account updatedAccount = accountRepository.save(account);

        return AccountDTO.builder()
                .accountId(updatedAccount.getAccountId())
                .accountName(updatedAccount.getAccountName())
                .email(updatedAccount.getEmail())
                .roleId(updatedAccount.getRole().getRoleId())
                .roleName(updatedAccount.getRole().getRoleName())
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        // Simply check if email exists and log success
        // Don't throw exception - let it pass silently for security
        Optional<Account> accountOpt = accountRepository.findByEmail(request.getEmail());
        if (accountOpt.isPresent()) {
            System.out.println("Password reset requested for: " + request.getEmail());
            System.out.println("Email exists - proceeding with reset flow");
        } else {
            System.out.println("Password reset requested for non-existent email: " + request.getEmail());
        }
        // Always succeed for security reasons (don't reveal if email exists)
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        // Extract email from the token (base64 decoded)
        String email;
        try {
            email = new String(java.util.Base64.getDecoder().decode(request.getToken()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token format");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired token"));

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }
}
