package com.triply.tripapp.service;

import com.triply.tripapp.dto.request.LoginRequest;
import com.triply.tripapp.dto.request.RegisterRequest;
import com.triply.tripapp.dto.request.SocialLoginRequest;
import com.triply.tripapp.dto.response.AuthResponse;
import com.triply.tripapp.dto.response.CustomerInfo;
import com.triply.tripapp.entity.Account;
import com.triply.tripapp.entity.Customer;
import com.triply.tripapp.entity.SocialProvider;
import com.triply.tripapp.repository.AccountRepository;
import com.triply.tripapp.repository.CustomerRepository;
import com.triply.tripapp.util.JwtUtil;
import com.triply.tripapp.util.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GoogleOAuth2Service googleOAuth2Service;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng!");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty() && customerRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Số điện thoại đã được sử dụng!");
        }
        if (accountRepository.existsByUserName(request.getUserName())) {
            throw new BadRequestException("Tên đăng nhập đã được sử dụng!");
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        Customer savedCustomer = customerRepository.save(customer);

        Account account = new Account();
        account.setUserName(request.getUserName());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setSocialProvider(SocialProvider.LOCAL);
        account.setCustomerId(savedCustomer.getCustomerId());
        Account savedAccount = accountRepository.save(account);

        String jwt = jwtUtil.generateToken(savedAccount.getUserName());
        CustomerInfo customerInfo = createCustomerInfo(savedCustomer, savedAccount);
        return new AuthResponse(jwt, "Bearer", jwtUtil.getExpirationTime(), customerInfo);
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUserName();
        String principal = identifier;

        if (isEmail(identifier)) {
            Optional<Account> localByEmail = accountRepository
                    .findByCustomerEmailAndSocialProvider(identifier, SocialProvider.LOCAL);
            if (localByEmail.isPresent()) {
                principal = localByEmail.get().getUserName();
            } else {
                // Fallback: if email is used directly as username for local account
                Optional<Account> byUsername = accountRepository.findByUserName(identifier);
                if (byUsername.isPresent() && (byUsername.get().getSocialProvider() == null
                        || byUsername.get().getSocialProvider() == SocialProvider.LOCAL)) {
                    principal = identifier;
                }
            }
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(principal, request.getPassword()));
        Account account = accountRepository.findByUserName(principal)
                .orElseThrow(() -> new BadRequestException("Tài khoản không tồn tại"));
        String jwt = jwtUtil.generateToken(account.getUserName());
        CustomerInfo customerInfo = createCustomerInfo(account.getCustomer(), account);
        return new AuthResponse(jwt, "Bearer", jwtUtil.getExpirationTime(), customerInfo);
    }

    private boolean isEmail(String value) {
        if (value == null) return false;
        return value.contains("@") && value.contains(".");
    }

    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        if (!"GOOGLE".equalsIgnoreCase(request.getProvider())) {
            throw new BadRequestException("Unsupported social provider: " + request.getProvider());
        }
        return authenticateWithGoogle(request);
    }

    private AuthResponse authenticateWithGoogle(SocialLoginRequest request) {
        GoogleOAuth2Service.GoogleUserInfo info = googleOAuth2Service.verifyGoogleToken(request.getToken());
        if (info.getEmailVerified() == null || !info.getEmailVerified()) {
            throw new BadRequestException("Google email not verified");
        }

        Optional<Customer> existingCustomer = customerRepository.findByEmail(info.getEmail());
        Customer customer;
        Account account;

        if (existingCustomer.isPresent()) {
            customer = existingCustomer.get();
            Optional<Account> existingGoogleAccount = accountRepository.findByCustomerEmailAndSocialProvider(info.getEmail(), SocialProvider.GOOGLE);
            if (existingGoogleAccount.isPresent()) {
                account = existingGoogleAccount.get();
            } else {
                account = new Account();
                account.setUserName(info.getEmail());
                account.setSocialProvider(SocialProvider.GOOGLE);
                account.setCustomerId(customer.getCustomerId());
                account = accountRepository.save(account);
            }
        } else {
            customer = new Customer();
            customer.setFullName(info.getName());
            customer.setEmail(info.getEmail());
            customer = customerRepository.save(customer);

            account = new Account();
            account.setUserName(info.getEmail());
            account.setPasswordHash("");
            account.setSocialProvider(SocialProvider.GOOGLE);
            account.setCustomerId(customer.getCustomerId());
            account = accountRepository.save(account);
        }

        String jwt = jwtUtil.generateToken(account.getUserName());
        CustomerInfo customerInfo = createCustomerInfo(customer, account);
        return new AuthResponse(jwt, "Bearer", jwtUtil.getExpirationTime(), customerInfo);
    }

    public CustomerInfo getCurrentUser(String userName) {
        Account account = accountRepository.findByUserName(userName)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return createCustomerInfo(account.getCustomer(), account);
    }

    private CustomerInfo createCustomerInfo(Customer customer, Account account) {
        return new CustomerInfo(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                account.getUserName(),
                account.getSocialProvider() == null ? null : account.getSocialProvider().name()
        );
    }
}


