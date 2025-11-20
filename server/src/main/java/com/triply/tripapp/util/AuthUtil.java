package com.triply.tripapp.util;

import com.triply.tripapp.entity.Account;
import com.triply.tripapp.entity.Role;
import com.triply.tripapp.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    private AccountRepository accountRepository;

    public Integer getCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Unauthorized: Authentication required");
        }
        String userName = authentication.getName();
        Account account = accountRepository.findByUserName(userName)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return account.getCustomerId();
    }

    public Account getAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Unauthorized: Authentication required");
        }
        String userName = authentication.getName();
        return accountRepository.findByUserName(userName)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    public boolean isAdmin(Authentication authentication) {
        Account account = getAccount(authentication);
        return account.getRole() == Role.ADMIN;
    }

    public void requireAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new BadRequestException("Forbidden: Admin role required");
        }
    }
}



