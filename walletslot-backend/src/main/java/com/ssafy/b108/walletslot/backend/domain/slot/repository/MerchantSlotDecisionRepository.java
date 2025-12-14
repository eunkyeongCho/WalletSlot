package com.ssafy.b108.walletslot.backend.domain.slot.repository;

import com.ssafy.b108.walletslot.backend.domain.slot.entity.MerchantSlotDecision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantSlotDecisionRepository extends JpaRepository<MerchantSlotDecision, String> {
    MerchantSlotDecision findByMerchantName(String transactionSummary);
}
