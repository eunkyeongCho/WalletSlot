package com.ssafy.b108.walletslot.backend.domain.slot.repository;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    Optional<Slot> findByUuid(String uuid);
    Slot findByName(String name); // 이거는 못찾아도 에러 발생시키면 안돼서 그냥 Slot 객체 반환하도록 작성
    List<Slot> findByIdNot(Long id);
}
