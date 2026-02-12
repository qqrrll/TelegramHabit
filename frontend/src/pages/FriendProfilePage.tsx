import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getFriendHabits, getFriendProfile, resolveAssetUrl } from "../api";
import { SkeletonList } from "../components/Skeleton";
import type { FriendResponse, HabitResponse } from "../types";

function displayName(friend: FriendResponse): string {
  const full = [friend.firstName, friend.lastName].filter(Boolean).join(" ").trim();
  if (full) return full;
  if (friend.username) return `@${friend.username}`;
  return "User";
}

export function FriendProfilePage() {
  const { t } = useTranslation();
  const { friendId } = useParams();
  const [friend, setFriend] = useState<FriendResponse | null>(null);
  const [habits, setHabits] = useState<HabitResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!friendId) return;
    setLoading(true);
    setError(null);
    Promise.all([getFriendProfile(friendId), getFriendHabits(friendId)])
      .then(([profile, habitsList]) => {
        setFriend(profile);
        setHabits(habitsList.filter((habit) => !habit.archived));
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [friendId]);

  if (loading) return <SkeletonList rows={3} />;
  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;
  if (!friend) return <p className="px-1 py-2 text-sm text-rose-600">{t("friendProfileUnavailable")}</p>;

  const avatar = resolveAssetUrl(friend.photoUrl);

  return (
    <section className="space-y-3 pb-8">
      <article className="glass-card p-4">
        <div className="flex items-center gap-3">
          {avatar ? (
            <img src={avatar} alt={displayName(friend)} className="h-14 w-14 rounded-full object-cover" />
          ) : (
            <div className="grid h-14 w-14 place-items-center rounded-full bg-white text-xl shadow-sm">👤</div>
          )}
          <div>
            <h2 className="text-lg font-black text-ink">{displayName(friend)}</h2>
            <p className="text-xs text-slate-400">{friend.username ? `@${friend.username}` : t("noUsername")}</p>
          </div>
        </div>
      </article>

      <article className="glass-card p-4">
        <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">{t("friendHabits")}</p>
        {habits.length === 0 && <p className="text-sm text-slate-500">{t("friendNoHabits")}</p>}
        <div className="space-y-2">
          {habits.map((habit) => (
            <div key={habit.id} className="flex items-center justify-between gap-2 rounded-2xl bg-white/70 p-3">
              <div className="min-w-0">
                <p className="truncate text-sm font-bold text-ink">
                  {habit.icon} {habit.title}
                </p>
                <p className="text-xs text-slate-400">
                  {habit.currentStreak} • {t("bestShort", { count: habit.bestStreak })}
                </p>
              </div>
              <Link
                className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm"
                to={`/friends/${friend.id}/habits/${habit.id}/stats`}
              >
                {t("stats")}
              </Link>
            </div>
          ))}
        </div>
      </article>
    </section>
  );
}

