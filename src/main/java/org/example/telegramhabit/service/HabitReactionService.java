package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.HabitReactionSummaryResponse;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.HabitReactionEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.HabitReactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HabitReactionService {

    private static final Set<String> ALLOWED_EMOJI = Set.of("🔥", "💪", "👏", "❤️", "🎯", "🚀");

    private final HabitReactionRepository habitReactionRepository;
    private final FriendService friendService;
    private final HabitService habitService;

    @Transactional(readOnly = true)
    public List<HabitReactionSummaryResponse> listForFriendHabit(UserEntity currentUser, UUID friendId, UUID habitId) {
        UserEntity friend = friendService.requireFriend(currentUser, friendId);
        HabitEntity habit = habitService.requireOwnedHabit(friend, habitId);
        return summary(currentUser, habit);
    }

    @Transactional
    public List<HabitReactionSummaryResponse> toggleForFriendHabit(UserEntity currentUser, UUID friendId, UUID habitId, String emoji) {
        UserEntity friend = friendService.requireFriend(currentUser, friendId);
        HabitEntity habit = habitService.requireOwnedHabit(friend, habitId);
        String normalizedEmoji = normalizeEmoji(emoji);

        habitReactionRepository.findByHabitAndReactorAndEmoji(habit, currentUser, normalizedEmoji)
                .ifPresentOrElse(
                        habitReactionRepository::delete,
                        () -> {
                            HabitReactionEntity reaction = new HabitReactionEntity();
                            reaction.setId(UUID.randomUUID());
                            reaction.setHabit(habit);
                            reaction.setReactor(currentUser);
                            reaction.setEmoji(normalizedEmoji);
                            reaction.setCreatedAt(LocalDateTime.now());
                            habitReactionRepository.save(reaction);
                        }
                );

        return summary(currentUser, habit);
    }

    @Transactional
    public List<HabitReactionSummaryResponse> removeForFriendHabit(UserEntity currentUser, UUID friendId, UUID habitId, String emoji) {
        UserEntity friend = friendService.requireFriend(currentUser, friendId);
        HabitEntity habit = habitService.requireOwnedHabit(friend, habitId);
        String normalizedEmoji = normalizeEmoji(emoji);

        habitReactionRepository.findByHabitAndReactorAndEmoji(habit, currentUser, normalizedEmoji)
                .ifPresent(habitReactionRepository::delete);

        return summary(currentUser, habit);
    }

    private List<HabitReactionSummaryResponse> summary(UserEntity currentUser, HabitEntity habit) {
        Set<String> mine = habitReactionRepository.findByHabitAndReactor(habit, currentUser).stream()
                .map(HabitReactionEntity::getEmoji)
                .collect(java.util.stream.Collectors.toSet());

        return habitReactionRepository.countByHabit(habit).stream()
                .map(item -> new HabitReactionSummaryResponse(item.getEmoji(), item.getCount(), mine.contains(item.getEmoji())))
                .sorted(Comparator.comparingLong(HabitReactionSummaryResponse::count).reversed()
                        .thenComparing(HabitReactionSummaryResponse::emoji))
                .toList();
    }

    private String normalizeEmoji(String emoji) {
        if (emoji == null) {
            throw new IllegalArgumentException("Emoji is required");
        }
        String normalized = emoji.trim();
        if (!ALLOWED_EMOJI.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported reaction");
        }
        return normalized;
    }
}
