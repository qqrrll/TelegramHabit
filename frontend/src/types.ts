export type HabitType = "DAILY" | "WEEKLY";

export interface AuthResponse {
  token: string;
  userId: string;
  firstName: string | null;
  username: string | null;
}

export interface HabitResponse {
  id: string;
  title: string;
  type: HabitType;
  timesPerWeek: number | null;
  color: string;
  icon: string;
  imageUrl: string | null;
  archived: boolean;
  currentStreak: number;
  bestStreak: number;
  createdAt: string;
}

export interface HabitRequest {
  title: string;
  type: HabitType;
  timesPerWeek: number | null;
  color: string;
  icon: string;
  archived: boolean;
}

export interface ActivityResponse {
  id: string;
  habitId: string | null;
  userId: string;
  actorName: string;
  actorPhotoUrl: string | null;
  ownEvent: boolean;
  type: "COMPLETED" | "STREAK" | "RECORD";
  message: string;
  createdAt: string;
}

export interface FriendResponse {
  id: string;
  username: string | null;
  firstName: string | null;
  lastName: string | null;
  photoUrl: string | null;
}

export interface FriendInviteResponse {
  code: string;
  inviteUrl: string;
  expiresAt: string;
}

export interface HabitStatsResponse {
  completedThisWeek: number;
  targetThisWeek: number;
  completedThisMonth: number;
  targetThisMonth: number;
  completionPercentWeek: number;
  completionPercentMonth: number;
  currentStreak: number;
  bestStreak: number;
}

export interface HabitCompletionResponse {
  id: string;
  date: string;
  completed: boolean;
  createdAt: string;
}

export interface UserProfileResponse {
  id: string;
  telegramId: number;
  username: string | null;
  firstName: string | null;
  lastName: string | null;
  photoUrl: string | null;
  language: "ru" | "en";
}
