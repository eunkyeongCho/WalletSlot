package com.ssafy.b108.walletslot.backend.domain.transaction.repository;

import com.ssafy.b108.walletslot.backend.domain.slot.entity.AccountSlot;
import com.ssafy.b108.walletslot.backend.domain.transaction.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = """
            SELECT t.* FROM transaction t JOIN account a ON t.account_id = a.id
            WHERE (a.uuid = :accountUuid) AND (:cursor IS NULL OR t.transaction_at < :cursor)
            ORDER BY t.transaction_at DESC
            LIMIT :size
    """, nativeQuery = true)
    List<Transaction> findByAccountUuid(@Param("accountUuid") String accountUuid, @Param("cursor") LocalDateTime cursor, @Param("size") int size);

    @Query(value = """
            SELECT t.* FROM transaction t JOIN account_slot a ON t.account_slot_id = a.id
            WHERE (a.uuid = :accountSlotUuid) AND (:cursor IS NULL OR t.transaction_at < :cursor)
            ORDER BY t.transaction_at DESC
            LIMIT :size
    """, nativeQuery = true)
    List<Transaction> findByAccountSlotUuid(@Param("accountSlotUuid") String accountSlotUuid, @Param("cursor") LocalDateTime cursor, @Param("size") int size);

    @Query(value = """
            SELECT t FROM Transaction t JOIN t.accountSlot a
            WHERE (a.uuid = :accountSlotUuid) AND (t.transactionAt > :startDate)
            ORDER BY t.transactionAt ASC
    """)
    List<Transaction> findByAccountSlotUuidForGraph(@Param("accountSlotUuid") String accountSlotUuid, @Param("startDate") LocalDateTime startDate);

    @Query("""
           select t
           from Transaction t
           where t.account.id = :accountId
             and t.transactionAt between :startAt and :endAt
           order by t.transactionAt asc
           """)
    List<Transaction> findByAccountIdAndTransactionAtBetween(
            @Param("accountId") Long accountId,
            @Param("startAt") LocalDateTime startInclusive,
            @Param("endAt")   LocalDateTime endInclusive
    );

    List<Transaction> findByAccountSlot(AccountSlot accountSlot);
    Optional<Transaction> findByUuid(String transactionUuid);

    @Query("select t.id from Transaction t where t.uuid = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") String uuid);

    @Query("select t.uuid from Transaction t where t.id = :id")
    Optional<String> findUuidById(@Param("id") Long id);
}
