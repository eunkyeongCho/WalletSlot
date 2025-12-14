package com.ssafy.b108.walletslot.backend.domain.ai_report.repository;

import com.ssafy.b108.walletslot.backend.domain.ai_report.entity.AiReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {

    Optional<AiReport> findByUuid(String uuid);

    // 해당 계좌(UUID)의 레포트가 존재하는 연-월 목록 (최근→과거)
    @Query(value = """
        SELECT DATE_FORMAT(ar.created_at, '%Y-%m') AS ym
        FROM ai_report ar
        JOIN account a ON a.id = ar.account_id
        WHERE a.user_id = :userId
          AND a.uuid = :accountUuid
        GROUP BY ym
        ORDER BY ym DESC
        """, nativeQuery = true)
    List<String> findAvailableYearMonths(@Param("userId") long userId,
                                         @Param("accountUuid") String accountUuid);

    // 특정 연-월 구간의 레포트 목록 (최근→과거)
    @Query(value = """
        SELECT ar.*
        FROM ai_report ar
        JOIN account a ON a.id = ar.account_id
        WHERE a.user_id = :userId
          AND a.uuid = :accountUuid
          AND ar.created_at >= :start
          AND ar.created_at <  :end
        ORDER BY ar.created_at DESC
        """, nativeQuery = true)
    List<AiReport> findByMonth(@Param("userId") long userId,
                               @Param("accountUuid") String accountUuid,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    boolean existsByAccount_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long accountId, LocalDateTime start, LocalDateTime end
    );
}
