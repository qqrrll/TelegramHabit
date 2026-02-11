import { useEffect, useMemo, useRef, useState, type TouchEventHandler } from "react";
import { useTranslation } from "react-i18next";
import { apiRequest } from "../api";
import { hapticImpact } from "../telegram";
import { SkeletonList } from "../components/Skeleton";
import type { ActivityResponse } from "../types";

function groupByDate(items: ActivityResponse[]): Array<{ date: string; items: ActivityResponse[] }> {
  const map = new Map<string, ActivityResponse[]>();
  for (const item of items) {
    const date = new Date(item.createdAt).toLocaleDateString();
    const list = map.get(date) ?? [];
    list.push(item);
    map.set(date, list);
  }
  return Array.from(map.entries()).map(([date, grouped]) => ({ date, items: grouped }));
}

function relativeTime(ts: string, locale: string): string {
  const now = Date.now();
  const target = new Date(ts).getTime();
  const diffSec = Math.round((target - now) / 1000);
  const abs = Math.abs(diffSec);
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: "auto" });
  if (abs < 60) return rtf.format(diffSec, "second");
  const min = Math.round(diffSec / 60);
  if (Math.abs(min) < 60) return rtf.format(min, "minute");
  const hr = Math.round(min / 60);
  if (Math.abs(hr) < 24) return rtf.format(hr, "hour");
  const day = Math.round(hr / 24);
  return rtf.format(day, "day");
}

export function ActivityPage() {
  const { t, i18n } = useTranslation();
  const [items, setItems] = useState<ActivityResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pullDistance, setPullDistance] = useState(0);
  const touchStartY = useRef<number | null>(null);
  const pulling = useRef(false);

  const loadActivity = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiRequest<ActivityResponse[]>("/api/activity");
      setItems(data);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadActivity();
  }, []);

  const grouped = useMemo(() => groupByDate(items), [items]);

  const onTouchStart: TouchEventHandler<HTMLElement> = (e) => {
    if (window.scrollY > 0) return;
    touchStartY.current = e.touches[0].clientY;
    pulling.current = true;
  };

  const onTouchMove: TouchEventHandler<HTMLElement> = (e) => {
    if (!pulling.current || touchStartY.current == null) return;
    const delta = e.touches[0].clientY - touchStartY.current;
    if (delta > 0) setPullDistance(Math.min(80, delta * 0.55));
  };

  const onTouchEnd: TouchEventHandler<HTMLElement> = () => {
    const shouldRefresh = pullDistance > 48;
    setPullDistance(0);
    pulling.current = false;
    touchStartY.current = null;
    if (shouldRefresh) {
      hapticImpact("light");
      void loadActivity();
    }
  };

  if (loading) return <SkeletonList rows={3} />;
  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;

  return (
    <section className="space-y-3 pb-8" onTouchStart={onTouchStart} onTouchMove={onTouchMove} onTouchEnd={onTouchEnd}>
      <div
        className="grid overflow-hidden text-center text-xs font-semibold text-slate-400 transition-all duration-200"
        style={{ height: `${pullDistance}px`, opacity: pullDistance > 8 ? 1 : 0 }}
      >
        <span className="self-end pb-2">{pullDistance > 48 ? t("releaseToRefresh") : t("pullToRefresh")}</span>
      </div>
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">{t("activityFeed")}</h2>
        <p className="mt-1 text-xs text-slate-400">{t("activitySubtitle")}</p>
      </div>

      {items.length === 0 && <div className="glass-card p-4 text-sm text-slate-500">{t("noEvents")}</div>}

      {grouped.map((section) => (
        <div key={section.date} className="space-y-2">
          <p className="px-1 text-xs font-semibold uppercase tracking-wide text-slate-400">{section.date}</p>
          {section.items.map((item, idx) => (
            <article key={item.id} className="glass-card card-enter p-4" style={{ animationDelay: `${idx * 40}ms` }}>
              <div className="flex items-center justify-between">
                <div className="rounded-full bg-white/90 px-3 py-1 text-xs font-bold text-ink shadow-sm">
                  {item.type === "COMPLETED" ? t("completed") : item.type === "RECORD" ? t("record") : t("streak")}
                </div>
                <time className="text-xs text-slate-400">{relativeTime(item.createdAt, i18n.language)}</time>
              </div>
              <p className="mt-3 text-sm font-medium text-slate-700">{item.message}</p>
            </article>
          ))}
        </div>
      ))}
    </section>
  );
}
