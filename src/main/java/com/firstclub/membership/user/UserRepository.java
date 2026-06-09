package com.firstclub.membership.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByExternalId(String externalId);

    @Query("select u.id from UserAccount u")
    List<Long> findAllIds();
}
