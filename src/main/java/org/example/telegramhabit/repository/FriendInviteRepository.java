package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.FriendInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** JPA access for one-time friend invite codes. */
public interface FriendInviteRepository extends JpaRepository<FriendInviteEntity, UUID> {
    Optional<FriendInviteEntity> findByCode(String code);
}
