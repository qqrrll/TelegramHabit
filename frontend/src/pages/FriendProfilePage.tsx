import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getFriendHabits, getFriendProfile, getHabitReactions, resolveAssetUrl, toggleHabitReaction } from "../api";
import { hapticImpact } from "../telegram";
import { ImageLightbox } from "../components/ImageLightbox";
import { SkeletonList } from "../components/Skeleton";
import type { FriendResponse, HabitReactionSummaryResponse, HabitResponse } from "../types";

const reactionButtons = ["🔥", "💪", "👏", "❤️", "🎯", "🚀"] as const;

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
  const [reactionsByHabit, setReactionsByHabit] = useState<Record<string, HabitReactionSummaryResponse[]>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [imagePreview, setImagePreview] = useState<{ src: string; alt: string } | null>(null);

  useEffect(() => {
    if (!friendId) return;
    setLoading(true);
    setError(null);
    Promise.all([getFriendProfile(friendId), getFriendHabits(friendId)])
      .then(([profile, habitsList]) => {
        setFriend(profile);
        const visibleHabits = habitsList.filter((habit) => !habit.archived);
        setHabits(visibleHabits);
        return visibleHabits;
      })
      .then(async (visibleHabits) => {
        const entries = await Promise.all(
          visibleHabits.map(async (habit) => {
            const reactions = await getHabitReactions(friendId, habit.id);
            return [habit.id, reactions] as const;
          })
        );
        setReactionsByHabit(Object.fromEntries(entries));
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [friendId]);

  const onReact = async (habitId: string, emoji: string) => {
    if (!friendId) return;
    try {
      hapticImpact("light");
      const updated = await toggleHabitReaction(friendId, habitId, emoji);
      setReactionsByHabit((prev) => ({ ...prev, [habitId]: updated }));
    } catch (e) {
      setError((e as Error).message);
    }
  };

  if (loading) return <SkeletonList rows={3} />;
  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;
  if (!friend) return <p className="px-1 py-2 text-sm text-rose-600">{t("friendProfileUnavailable")}</p>;

  const avatar = resolveAssetUrl(friend.photoUrl);

  return (
    <section className="space-y-3 pb-8">
      {imagePreview && <ImageLightbox src={imagePreview.src} alt={imagePreview.alt} onClose={() => setImagePreview(null)} />}
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
        {friend.username && (
          <a
            href={`https://t.me/${friend.username}`}
            target="_blank"
            rel="noreferrer"
            className="tap mt-3 inline-flex rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-sky-600 shadow-sm"
          >
            {t("messageFriend")}
          </a>
        )}
      </article>

      <article className="glass-card p-4">
        <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">{t("friendHabits")}</p>
        {habits.length === 0 && <p className="text-sm text-slate-500">{t("friendNoHabits")}</p>}
        <div className="space-y-2">
          {habits.map((habit) => (
            <div key={habit.id} className="rounded-2xl bg-white/70 p-3">
              <div className="flex items-center justify-between gap-2">
                <div className="flex min-w-0 items-center gap-2">
                  {habit.imageUrl ? (
                    <button
                      type="button"
                      className="tap"
                      onClick={() => {
                        const src = resolveAssetUrl(habit.imageUrl);
                        if (src) {
                          setImagePreview({ src, alt: habit.title });
                        }
                      }}
                    >
                      <img src={resolveAssetUrl(habit.imageUrl) ?? ""} alt={habit.title} className="h-9 w-9 rounded-lg object-cover" />
                    </button>
                  ) : (
                    <div className="grid h-9 w-9 place-items-center rounded-lg bg-white text-lg shadow-sm">{habit.icon}</div>
                  )}
                  <div className="min-w-0">
                    <p className="truncate text-sm font-bold text-ink">{habit.title}</p>
                    <p className="text-xs text-slate-400">
                      {habit.currentStreak} • {t("bestShort", { count: habit.bestStreak })}
                    </p>
                  </div>
                </div>
                <Link
                  className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm"
                  to={`/friends/${friend.id}/habits/${habit.id}/stats`}
                >
                  {t("stats")}
                </Link>
              </div>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {reactionButtons.map((emoji) => {
                  const reaction = (reactionsByHabit[habit.id] ?? []).find((item) => item.emoji === emoji);
                  const count = reaction?.count ?? 0;
                  const mine = reaction?.mine ?? false;
                  return (
                    <button
                      key={emoji}
                      type="button"
                      onClick={() => void onReact(habit.id, emoji)}
                      className={`tap rounded-full px-2 py-1 text-xs font-semibold transition-all duration-200 ${
                        mine ? "bg-ink text-white" : "bg-white/90 text-slate-600"
                      }`}
                    >
                      {emoji} {count > 0 ? count : ""}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </article>
    </section>
  );
}
