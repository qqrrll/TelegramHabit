package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.StoredImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredImageRepository extends JpaRepository<StoredImageEntity, UUID> {
}
