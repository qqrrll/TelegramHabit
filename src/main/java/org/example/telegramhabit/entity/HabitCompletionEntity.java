package org.example.telegramhabit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Stores completion marks by date for each habit. */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "habit")
@NoArgsConstructor
@Entity
@Table(name = "habit_completion")
public class HabitCompletionEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private HabitEntity habit;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
