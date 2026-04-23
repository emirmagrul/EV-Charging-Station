package com.ev.repository;

import com.ev.model.ConnectorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectorTypeRepository extends JpaRepository<ConnectorType, Long> {
}
