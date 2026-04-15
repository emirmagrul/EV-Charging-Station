package com.ev.repository;

import com.ev.model.EVDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EVDriverRepository extends JpaRepository<EVDriver, Long> {
    Optional<EVDriver> findByEmail(String email); // Giriş işlemleri için
}
