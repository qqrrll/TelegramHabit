package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;

/** Accepts friend invite by code (from link or manual input). */
public record AcceptFriendInviteRequest(@NotBlank String code) {
}
