package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.FriendshipEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA access for friendship edges. */
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {
    List<FriendshipEntity> findByUser(UserEntity user);

    Optional<FriendshipEntity> findByUserAndFriend(UserEntity user, UserEntity friend);

    void deleteByUserAndFriend(UserEntity user, UserEntity friend);
}
