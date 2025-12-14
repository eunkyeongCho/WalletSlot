package com.ssafy.b108.walletslot.backend.domain.slot.repository;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.AccountSlot;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountSlotRepository extends JpaRepository<AccountSlot, Long> {

    List<AccountSlot> findByAccount(Account account);
    void deleteByAccount(Account account);
    Optional<AccountSlot> findByAccountAndSlot(Account account, Slot slot);
    Optional<AccountSlot> findByUuid(String accountSlotUuid);
    void deleteByUuid(String accountSlotUuid);

    @Query("SELECT s.slot.uuid FROM AccountSlot s WHERE s.account.uuid = :accountUuid")
    Set<String> findSlotUuidsByAccountUuid(@Param("accountUuid") String accountUuid);

    @Query("SELECT s FROM AccountSlot s WHERE s.account.uuid=:accountUuid AND s.slot.id=0 ")
    Optional<AccountSlot> findUncategorizedAccountSlot(String accountUuid);
}
