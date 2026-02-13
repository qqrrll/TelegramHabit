import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getNotifications, markAllNotificationsRead, markNotificationRead, resolveAssetUrl } from "../api";
import { SkeletonList } from "../components/Skeleton";
import type { NotificationResponse } from "../types";

function relativeTime(ts: number, locale: string): string {
  const now = Date.now();
  const diffSec = Math.round((ts - now) / 1000);
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

export function NotificationsPage() {
  const { t, i18n } = useTranslation();
  const [items, setItems] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      setItems(await getNotifications());
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const readAll = async () => {
    try {
      await markAllNotificationsRead();
      setItems((prev) => prev.map((item) => ({ ...item, read: true })));
    } catch (e) {
      setError((e as Error).message);
    }
  };

  const readOne = async (id: string) => {
    try {
      await markNotificationRead(id);
      setItems((prev) => prev.map((item) => (item.id === id ? { ...item, read: true } : item)));
    } catch (e) {
      setError((e as Error).message);
    }
  };

  if (loading) return <SkeletonList rows={3} />;
  if (error) return <p className="px-1 py-2 text-sm text-rose-600">{error}</p>;

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <div className="flex items-center justify-between gap-2">
          <div>
            <h2 className="text-lg font-black text-ink">{t("notificationsTitle")}</h2>
            <p className="mt-1 text-xs text-slate-400">{t("notificationsSubtitle")}</p>
          </div>
          <button
            type="button"
            onClick={() => void readAll()}
            className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm"
          >
            {t("markAllRead")}
          </button>
        </div>
      </div>

      {items.length === 0 && <div className="glass-card p-4 text-sm text-slate-500">{t("noNotifications")}</div>}

      {items.map((item) => {
        const actorPhoto = resolveAssetUrl(item.actorPhotoUrl);
        return (
          <article
            key={item.id}
            className={`glass-card p-4 ${item.read ? "opacity-85" : "ring-1 ring-sky-200"}`}
          >
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                {actorPhoto ? (
                  <img src={actorPhoto} alt={item.actorName} className="h-8 w-8 rounded-full object-cover" />
                ) : (
                  <div className="grid h-8 w-8 place-items-center rounded-full bg-white/90 text-sm shadow-sm">👤</div>
                )}
                <div>
                  <p className="text-xs font-bold text-ink">{item.actorName}</p>
                  <p className="text-xs text-slate-500">{item.message}</p>
                </div>
              </div>
              <time className="text-xs text-slate-400">{relativeTime(item.createdAtEpochMs, i18n.language)}</time>
            </div>
            <div className="mt-3 flex items-center gap-2">
              {item.activityId && (
                <Link className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm" to="/activity">
                  {t("openFeed")}
                </Link>
              )}
              {!item.read && (
                <button
                  type="button"
                  onClick={() => void readOne(item.id)}
                  className="tap rounded-full bg-sky-50 px-3 py-1.5 text-xs font-semibold text-sky-700 shadow-sm"
                >
                  {t("markRead")}
                </button>
              )}
            </div>
          </article>
        );
      })}
    </section>
  );
}
