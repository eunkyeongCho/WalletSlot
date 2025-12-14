package com.ssafy.b108.walletslot.backend.domain.notification.repository;

import com.ssafy.b108.walletslot.backend.domain.notification.entity.PushEndpoint;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushEndpointRepository extends JpaRepository<PushEndpoint, Long> {
    List<PushEndpoint> findByUserOrderByIdDesc(User user);
    Optional<PushEndpoint> findByUserAndDeviceId(User user, String deviceId);
    Optional<PushEndpoint> findByUser(User user);
}
