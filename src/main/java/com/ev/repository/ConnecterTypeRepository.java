package com.ev.repository;

import com.ev.model.ConnecterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnecterTypeRepository extends JpaRepository<ConnecterType, Long> {
}
