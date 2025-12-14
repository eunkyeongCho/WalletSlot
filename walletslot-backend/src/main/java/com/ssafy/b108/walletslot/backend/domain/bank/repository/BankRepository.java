package com.ssafy.b108.walletslot.backend.domain.bank.repository;

import com.ssafy.b108.walletslot.backend.domain.bank.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByUuid(String uuid);
    Optional<Bank> findByCode(String code);

    @Query("SELECT b.code FROM Bank b WHERE b.uuid IN :bankUuids")
    Set<String> findCodesByUuids(@Param("bankUuids") List<String> bankUuids);
}
