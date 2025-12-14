package com.ssafy.b108.walletslot.backend.domain.account.repository;

import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.slot.entity.Slot;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByUserAndIsPrimaryTrue(User user);
    Optional<Account> findByUuid(String uuid);
    void deleteByUuid(String uuid);

    Optional<Account> findByUserIdAndUuid(Long userId, String uuid);

    // 권한 체크 등에 유용 (선택)
    boolean existsByUuidAndUser_Id(String uuid, Long userId);
}
