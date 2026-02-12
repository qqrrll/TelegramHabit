package org.example.telegramhabit.dto;

/** Invite code and shareable URL for friend onboarding. */
public record FriendInviteResponse(String code, String inviteUrl, String expiresAt) {
}
