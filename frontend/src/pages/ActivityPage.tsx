import { useEffect, useState } from "react";
import { apiRequest } from "../api";
import type { ActivityResponse } from "../types";

const labelMap: Record<ActivityResponse["type"], string> = {
  COMPLETED: "Completed",
  STREAK: "Streak",
  RECORD: "Record"
};

export function ActivityPage() {
  const [items, setItems] = useState<ActivityResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiRequest<ActivityResponse[]>("/api/activity")
      .then(setItems)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="px-1 py-2 text-sm text-slate-500">Loading activity...</p>;
  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">Activity feed</h2>
        <p className="mt-1 text-xs text-slate-400">Track momentum, records and consistency.</p>
      </div>

      {items.length === 0 && <div className="glass-card p-4 text-sm text-slate-500">No events yet.</div>}

      {items.map((item) => (
        <article key={item.id} className="glass-card p-4">
          <div className="flex items-center justify-between">
            <div className="rounded-full bg-white/90 px-3 py-1 text-xs font-bold text-ink shadow-sm">{labelMap[item.type]}</div>
            <time className="text-xs text-slate-400">{new Date(item.createdAt).toLocaleString()}</time>
          </div>
          <p className="mt-3 text-sm font-medium text-slate-700">{item.message}</p>
        </article>
      ))}
    </section>
  );
}
