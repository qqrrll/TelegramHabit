import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiRequest } from "../api";
import type { HabitResponse } from "../types";

function ProgressBlocks({ value }: { value: number }) {
  const total = 7;
  const filled = Math.max(0, Math.min(total, value));
  return (
    <div className="mt-3 flex gap-1.5">
      {Array.from({ length: total }).map((_, idx) => (
        <div
          key={idx}
          className={`h-2.5 flex-1 rounded-full transition-all duration-200 ${
            idx < filled ? "bg-ink/80" : "bg-slate-200"
          }`}
        />
      ))}
    </div>
  );
}

export function HomePage() {
  const [habits, setHabits] = useState<HabitResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadHabits = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest<HabitResponse[]>("/api/habits");
      setHabits(data.filter((h) => !h.archived));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadHabits();
  }, []);

  const completeToday = async (id: string) => {
    await apiRequest(`/api/habits/${id}/complete`, { method: "POST" });
    await loadHabits();
  };

  if (loading) {
    return <p className="px-1 py-2 text-sm text-slate-500">Loading habits...</p>;
  }
  if (error) {
    return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;
  }

  return (
    <section className="space-y-3 pb-24">
      {habits.length === 0 && (
        <div className="glass-card px-4 py-6 text-center text-sm text-slate-500">No habits yet. Create your first one.</div>
      )}

      {habits.map((habit) => (
        <article
          key={habit.id}
          className="glass-card relative overflow-hidden border-white/80 p-4 shadow-md backdrop-blur transition-all duration-200"
          style={{
            background: `linear-gradient(140deg, ${habit.color}18 0%, rgba(255,255,255,0.72) 60%)`
          }}
        >
          <div className="pr-16">
            <div className="flex items-start gap-3">
              <div className="mt-0.5 text-2xl">{habit.icon}</div>
              <div className="min-w-0">
                <h3 className="truncate text-[17px] font-black text-ink">{habit.title}</h3>
                <p className="mt-0.5 text-xs text-slate-400">
                  {habit.currentStreak} day streak • best {habit.bestStreak}
                </p>
              </div>
            </div>

            <ProgressBlocks value={habit.currentStreak} />

            <div className="mt-3 flex items-center gap-2 text-xs font-semibold text-slate-500">
              <Link className="tap rounded-full bg-white/80 px-3 py-1.5 transition-all duration-200 hover:bg-white" to={`/habits/${habit.id}/stats`}>
                Stats
              </Link>
              <Link className="tap rounded-full bg-white/80 px-3 py-1.5 transition-all duration-200 hover:bg-white" to={`/habits/${habit.id}/edit`}>
                Edit
              </Link>
            </div>
          </div>

          <button
            type="button"
            aria-label={`Complete ${habit.title}`}
            className="tap absolute bottom-4 right-4 inline-flex h-12 w-12 items-center justify-center rounded-full bg-ink text-lg text-white shadow-float transition-all duration-200 hover:scale-105"
            onClick={() => void completeToday(habit.id)}
          >
            ✓
          </button>
        </article>
      ))}
    </section>
  );
}
