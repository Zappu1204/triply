package com.triply.tripapp.repository;

import com.triply.tripapp.entity.Account;
import com.triply.tripapp.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUserName(String userName);

    @Query("SELECT a FROM Account a JOIN a.customer c WHERE c.email = :email")
    Optional<Account> findByCustomerEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a JOIN a.customer c WHERE c.email = :email AND a.socialProvider = :provider")
    Optional<Account> findByCustomerEmailAndSocialProvider(@Param("email") String email, @Param("provider") SocialProvider provider);

    boolean existsByUserName(String userName);
}


