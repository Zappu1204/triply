package com.triply.tripapp.repository;

import com.triply.tripapp.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    Optional<Budget> findByTripId(Integer tripId);
}



