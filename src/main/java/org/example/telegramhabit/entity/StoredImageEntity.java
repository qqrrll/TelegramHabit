package org.example.telegramhabit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity
@Table(name = "stored_image")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class StoredImageEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
