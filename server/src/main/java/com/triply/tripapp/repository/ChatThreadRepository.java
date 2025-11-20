package com.triply.tripapp.repository;

import com.triply.tripapp.entity.ChatThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThread, Integer> {
    List<ChatThread> findByCustomerIdOrderByLastMessageAtDesc(Integer customerId);
}



