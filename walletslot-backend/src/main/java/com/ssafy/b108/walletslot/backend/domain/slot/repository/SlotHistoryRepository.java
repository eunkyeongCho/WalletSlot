package com.ssafy.b108.walletslot.backend.domain.slot.repository;

import com.ssafy.b108.walletslot.backend.domain.slot.entity.AccountSlot;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.SlotHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlotHistoryRepository extends JpaRepository<SlotHistory, Long> {
    void deleteByAccountSlot(AccountSlot accountSlot);
    List<SlotHistory> findByAccountSlot(AccountSlot accountSlot);
}
