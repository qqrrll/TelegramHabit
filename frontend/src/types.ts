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
  type: "COMPLETED" | "STREAK" | "RECORD";
  message: string;
  createdAt: string;
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
