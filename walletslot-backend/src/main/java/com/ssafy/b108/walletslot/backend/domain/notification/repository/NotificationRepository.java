package com.ssafy.b108.walletslot.backend.domain.notification.repository;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.Notification;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByIdDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndTypeOrderByIdDesc(User user, Notification.Type type, Pageable pageable);

    Optional<Notification> findByUuidAndUser(String uuid, User user);

    @Query("""
           select n
             from Notification n
            where n.user = :user
              and (n.isDelivered is null or n.isDelivered = false)
            order by n.id asc
           """)
    List<Notification> findUndeliveredByUser(@Param("user") User user);

    Optional<Notification> findByIdAndUser(Long id, User user);

    long countByUserAndIsReadFalse(User user);

    long countByUserAndIsReadFalseAndType(User user, Notification.Type type);

    Page<Notification> findByUserAndIsReadFalseOrderByIdDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndTypeAndIsReadFalseOrderByIdDesc(User user, Notification.Type type, Pageable pageable);

    Optional<Notification> findFirstByUserAndTypeAndTxId(User user, Notification.Type type, Long txId);
}
