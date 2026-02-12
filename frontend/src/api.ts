import type {
  AuthResponse,
  FriendInviteResponse,
  FriendResponse,
  HabitResponse,
  HabitStatsResponse,
  UserProfileResponse
} from "./types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_KEY = "habit_jwt";

export function setToken(token: string): void {
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function clearToken(): void {
  sessionStorage.removeItem(TOKEN_KEY);
}

export function resolveAssetUrl(url: string | null): string | null {
  if (!url) {
    return null;
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE_URL}${url.startsWith("/") ? "" : "/"}${url}`;
}

export async function telegramAuth(initData: string): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/telegram`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ initData })
  });
  if (!response.ok) {
    let errorMessage = "Telegram auth failed";
    const raw = await response.text();
    try {
      const body = JSON.parse(raw) as { error?: string };
      if (body?.error) {
        errorMessage = body.error;
      } else if (raw) {
        errorMessage = raw;
      }
    } catch {
      if (raw) {
        errorMessage = raw;
      }
    }
    throw new Error(errorMessage);
  }
  return response.json() as Promise<AuthResponse>;
}

export async function devAuth(telegramId: number, firstName: string, username: string): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/dev`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ telegramId, firstName, username })
  });
  if (!response.ok) {
    throw new Error("Dev auth failed");
  }
  return response.json() as Promise<AuthResponse>;
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken();
  const headers = new Headers(init?.headers ?? {});
  const isFormData = init?.body instanceof FormData;
  if (!isFormData) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  if (!response.ok) {
    if (response.status === 401) {
      clearToken();
    }
    const errorText = await response.text();
    throw new Error(errorText || `API error: ${response.status}`);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export function getMyProfile(): Promise<UserProfileResponse> {
  return apiRequest<UserProfileResponse>("/api/users/me");
}

export function updateMyLanguage(language: "ru" | "en"): Promise<UserProfileResponse> {
  return apiRequest<UserProfileResponse>("/api/users/language", {
    method: "PATCH",
    body: JSON.stringify({ language })
  });
}

export function uploadMyAvatar(file: File): Promise<UserProfileResponse> {
  const formData = new FormData();
  formData.append("file", file);
  return apiRequest<UserProfileResponse>("/api/users/avatar", {
    method: "POST",
    body: formData
  });
}

export function getFriends(): Promise<FriendResponse[]> {
  return apiRequest<FriendResponse[]>("/api/friends");
}

export function createFriendInvite(): Promise<FriendInviteResponse> {
  return apiRequest<FriendInviteResponse>("/api/friends/invite", { method: "POST" });
}

export function acceptFriendInvite(code: string): Promise<FriendResponse> {
  return apiRequest<FriendResponse>("/api/friends/accept", {
    method: "POST",
    body: JSON.stringify({ code })
  });
}

export function removeFriend(friendId: string): Promise<void> {
  return apiRequest<void>(`/api/friends/${friendId}`, { method: "DELETE" });
}

export function getFriendProfile(friendId: string): Promise<FriendResponse> {
  return apiRequest<FriendResponse>(`/api/friends/${friendId}/profile`);
}

export function getFriendHabits(friendId: string): Promise<HabitResponse[]> {
  return apiRequest<HabitResponse[]>(`/api/friends/${friendId}/habits`);
}

export function getFriendHabitStats(friendId: string, habitId: string): Promise<HabitStatsResponse> {
  return apiRequest<HabitStatsResponse>(`/api/friends/${friendId}/habits/${habitId}/stats`);
}
