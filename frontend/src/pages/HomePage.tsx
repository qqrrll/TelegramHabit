import { useEffect, useRef, useState, type TouchEventHandler } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { apiRequest } from "../api";
import { hapticImpact } from "../telegram";
import { SkeletonList } from "../components/Skeleton";
import type { HabitCompletionResponse, HabitResponse } from "../types";

function weekDaysFromToday(now: Date): Date[] {
  const monday = new Date(now);
  const day = (now.getDay() + 6) % 7; // Monday=0 ... Sunday=6
  monday.setDate(now.getDate() - day);
  return Array.from({ length: 7 }).map((_, idx) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + idx);
    return d;
  });
}

function ProgressBlocks({ doneDates, animate }: { doneDates: Set<string>; animate: boolean }) {
  const now = new Date();
  const days = weekDaysFromToday(now);

  return (
    <div className="mt-3">
      <div className="flex gap-1.5">
        {days.map((d, idx) => {
          const isToday = d.toDateString() === now.toDateString();
          const key = d.toISOString().slice(0, 10);
          const done = doneDates.has(key);
          return (
            <div
              key={idx}
              title={d.toLocaleDateString()}
              className={`h-2.5 flex-1 rounded-full transition-all duration-300 ease-out ${
                done ? "bg-ink/80" : "bg-slate-200"
              } ${animate && done ? "scale-105 bg-emerald-500/80" : ""} ${
                isToday ? "ring-2 ring-sky-300" : ""
              }`}
            />
          );
        })}
      </div>
      <div className="mt-1.5 flex gap-1.5">
        {days.map((d, idx) => {
          const isToday = d.toDateString() === now.toDateString();
          return (
            <span key={idx} className={`flex-1 text-center text-[10px] ${isToday ? "font-bold text-sky-600" : "text-slate-400"}`}>
              {d.getDate()}
            </span>
          );
        })}
      </div>
    </div>
  );
}

export function HomePage() {
  const { t } = useTranslation();
  const [habits, setHabits] = useState<HabitResponse[]>([]);
  const [habitWeekCompletions, setHabitWeekCompletions] = useState<Record<string, Set<string>>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [highlightedHabitId, setHighlightedHabitId] = useState<string | null>(null);
  const [pressedHabitId, setPressedHabitId] = useState<string | null>(null);
  const [pullDistance, setPullDistance] = useState(0);
  const touchStartY = useRef<number | null>(null);
  const pulling = useRef(false);

  const loadHabits = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest<HabitResponse[]>("/api/habits");
      const activeHabits = data.filter((h) => !h.archived);
      setHabits(activeHabits);

      const weekKeys = new Set(weekDaysFromToday(new Date()).map((d) => d.toISOString().slice(0, 10)));
      const histories = await Promise.all(
        activeHabits.map(async (habit) => {
          const history = await apiRequest<HabitCompletionResponse[]>(`/api/habits/${habit.id}/history`);
          const done = new Set(history.map((entry) => entry.date).filter((date) => weekKeys.has(date)));
          return [habit.id, done] as const;
        })
      );
      setHabitWeekCompletions(Object.fromEntries(histories));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadHabits();
  }, []);

  const onTouchStart: TouchEventHandler<HTMLElement> = (e) => {
    if (window.scrollY > 0) return;
    touchStartY.current = e.touches[0].clientY;
    pulling.current = true;
  };

  const onTouchMove: TouchEventHandler<HTMLElement> = (e) => {
    if (!pulling.current || touchStartY.current == null) return;
    const delta = e.touches[0].clientY - touchStartY.current;
    if (delta > 0) {
      setPullDistance(Math.min(80, delta * 0.55));
    }
  };

  const onTouchEnd: TouchEventHandler<HTMLElement> = () => {
    const shouldRefresh = pullDistance > 48;
    setPullDistance(0);
    pulling.current = false;
    touchStartY.current = null;
    if (shouldRefresh) {
      hapticImpact("light");
      void loadHabits();
    }
  };

  const completeToday = async (id: string) => {
    hapticImpact("medium");
    setPressedHabitId(id);
    setHighlightedHabitId(id);
    try {
      await new Promise((resolve) => setTimeout(resolve, 170));
      await apiRequest(`/api/habits/${id}/complete`, { method: "POST" });
      await loadHabits();
      window.setTimeout(() => setHighlightedHabitId(null), 320);
    } catch (e) {
      setError((e as Error).message);
      setHighlightedHabitId(null);
    } finally {
      setPressedHabitId(null);
    }
  };

  if (loading)
    return (
      <div className="space-y-3">
        <SkeletonList rows={3} />
      </div>
    );
  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;

  return (
    <section className="space-y-3 pb-24" onTouchStart={onTouchStart} onTouchMove={onTouchMove} onTouchEnd={onTouchEnd}>
      <div
        className="grid overflow-hidden text-center text-xs font-semibold text-slate-400 transition-all duration-200"
        style={{ height: `${pullDistance}px`, opacity: pullDistance > 8 ? 1 : 0 }}
      >
        <span className="self-end pb-2">{pullDistance > 48 ? t("releaseToRefresh") : t("pullToRefresh")}</span>
      </div>
      {habits.length === 0 && <div className="glass-card px-4 py-6 text-center text-sm text-slate-500">{t("noHabits")}</div>}

      {habits.map((habit, idx) => (
        <article
          key={habit.id}
          className={`glass-card card-enter relative overflow-hidden border-white/80 p-4 shadow-md backdrop-blur transition-all duration-200 ${
            highlightedHabitId === habit.id ? "ring-2 ring-emerald-300 shadow-[0_0_0_4px_rgba(16,185,129,0.18)]" : ""
          }`}
          style={{
            background: `linear-gradient(140deg, ${habit.color}18 0%, rgba(255,255,255,0.72) 60%)`,
            animationDelay: `${idx * 45}ms`
          }}
        >
          <div className="pr-16">
            <div className="flex items-start gap-3">
              <div className="mt-0.5 text-2xl">{habit.icon}</div>
              <div className="min-w-0">
                <h3 className="truncate text-[17px] font-black text-ink">{habit.title}</h3>
                <p className="mt-0.5 text-[11px] text-slate-400">
                  {habit.currentStreak} • {t("bestShort", { count: habit.bestStreak })}
                </p>
              </div>
            </div>

            <ProgressBlocks doneDates={habitWeekCompletions[habit.id] ?? new Set<string>()} animate={highlightedHabitId === habit.id} />

            <div className="mt-3 flex items-center gap-2 text-xs font-semibold text-slate-500">
              <Link className="tap rounded-full bg-white/70 px-3 py-1.5 text-[11px] transition-all duration-200 hover:bg-white" to={`/habits/${habit.id}/stats`}>
                📊 {t("stats")}
              </Link>
              <Link className="tap rounded-full bg-white/70 px-3 py-1.5 text-[11px] transition-all duration-200 hover:bg-white" to={`/habits/${habit.id}/edit`}>
                ✏️ {t("edit")}
              </Link>
            </div>
          </div>

          <button
            type="button"
            aria-label={`Complete ${habit.title}`}
            className={`tap absolute bottom-4 right-4 inline-flex h-12 w-12 items-center justify-center rounded-full bg-ink text-lg text-white shadow-float transition-transform duration-150 hover:scale-105 active:scale-95 ${
              pressedHabitId === habit.id ? "scale-95" : ""
            }`}
            onClick={() => void completeToday(habit.id)}
          >
            ✓
          </button>
        </article>
      ))}
    </section>
  );
}
