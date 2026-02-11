import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { apiRequest } from "../api";
import { SkeletonList } from "../components/Skeleton";
import type { HabitStatsResponse } from "../types";

function Ring({ percent, label }: { percent: number; label: string }) {
  const p = Math.max(0, Math.min(100, percent));
  return (
    <div className="glass-card flex flex-1 flex-col items-center justify-center p-4">
      <div
        className="grid h-20 w-20 place-items-center rounded-full text-sm font-black text-ink"
        style={{ background: `conic-gradient(#111827 ${p * 3.6}deg, #e2e8f0 0deg)` }}
      >
        <div className="grid h-14 w-14 place-items-center rounded-full bg-white">{p}%</div>
      </div>
      <p className="mt-2 text-xs font-semibold uppercase tracking-wide text-slate-400">{label}</p>
    </div>
  );
}

export function HabitStatsPage() {
  const { t } = useTranslation();
  const { id } = useParams();
  const [stats, setStats] = useState<HabitStatsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    apiRequest<HabitStatsResponse>(`/api/habits/${id}/stats`).then(setStats).catch((e: Error) => setError(e.message));
  }, [id]);

  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;
  if (!stats) return <SkeletonList rows={2} />;

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">{t("progressStats")}</h2>
        <p className="mt-1 text-xs text-slate-400">{t("progressSubtitle")}</p>
      </div>

      <div className="flex gap-3">
        <Ring percent={stats.completionPercentWeek} label={t("week")} />
        <Ring percent={stats.completionPercentMonth} label={t("month")} />
      </div>

      <article className="glass-card p-4">
        <div className="grid grid-cols-2 gap-3">
          <div className="rounded-2xl bg-white/70 p-3">
            <p className="text-xs uppercase tracking-wide text-slate-400">{t("currentStreak")}</p>
            <p className="mt-1 text-2xl font-black text-ink">{stats.currentStreak}</p>
          </div>
          <div className="rounded-2xl bg-white/70 p-3">
            <p className="text-xs uppercase tracking-wide text-slate-400">{t("bestStreak")}</p>
            <p className="mt-1 text-2xl font-black text-ink">{stats.bestStreak}</p>
          </div>
          <div className="rounded-2xl bg-white/70 p-3">
            <p className="text-xs uppercase tracking-wide text-slate-400">{t("weekDone")}</p>
            <p className="mt-1 text-lg font-black text-ink">
              {stats.completedThisWeek}/{stats.targetThisWeek}
            </p>
          </div>
          <div className="rounded-2xl bg-white/70 p-3">
            <p className="text-xs uppercase tracking-wide text-slate-400">{t("monthDone")}</p>
            <p className="mt-1 text-lg font-black text-ink">
              {stats.completedThisMonth}/{stats.targetThisMonth}
            </p>
          </div>
        </div>
      </article>
    </section>
  );
}
