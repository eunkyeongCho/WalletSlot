package com.ssafy.b108.walletslot.backend.domain.user.repository;

import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phone);

    @Query("select u from User u where u.baseDay = :day")
    List<User> findAllByBaseDay(@Param("day") Short day);

    @Query("select u from User u where u.baseDay > :lastDay")
    List<User> findAllByBaseDayGreaterThan(@Param("lastDay") Short lastDay);

}
