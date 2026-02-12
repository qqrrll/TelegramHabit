import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { apiRequest, resolveAssetUrl, uploadHabitImage } from "../api";
import { hapticImpact } from "../telegram";
import type { HabitRequest, HabitResponse, HabitType } from "../types";

const defaultForm: HabitRequest = {
  title: "",
  type: "DAILY",
  timesPerWeek: null,
  color: "#74b9ff",
  icon: "🔥",
  archived: false
};

const colors = ["#74b9ff", "#8be9b3", "#ffb36a", "#fca5a5", "#c4b5fd"];
const emojis = ["🔥", "💪", "📚", "🏃", "💧", "🧘", "🎯", "🥗", "🚴", "🛌", "🍎", "🎹"];

export function HabitFormPage() {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState<HabitRequest>(defaultForm);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [habitIdForImage, setHabitIdForImage] = useState<string | null>(id ?? null);
  const [habitImageUrl, setHabitImageUrl] = useState<string | null>(null);
  const [imageUploading, setImageUploading] = useState(false);
  const [pendingImageFile, setPendingImageFile] = useState<File | null>(null);
  const [pendingImagePreviewUrl, setPendingImagePreviewUrl] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    apiRequest<HabitResponse[]>("/api/habits")
      .then((items) => {
        const habit = items.find((h) => h.id === id);
        if (!habit) {
          setError("Habit not found");
          return;
        }
        setForm({
          title: habit.title,
          type: habit.type,
          timesPerWeek: habit.timesPerWeek,
          color: habit.color,
          icon: habit.icon,
          archived: habit.archived
        });
        setHabitIdForImage(habit.id);
        setHabitImageUrl(habit.imageUrl);
      })
      .catch((e: Error) => setError(e.message));
  }, [id]);

  useEffect(
    () => () => {
      if (pendingImagePreviewUrl) {
        URL.revokeObjectURL(pendingImagePreviewUrl);
      }
    },
    [pendingImagePreviewUrl]
  );

  const submit = async () => {
    setSaving(true);
    setError(null);
    try {
      hapticImpact("medium");
      const payload: HabitRequest = {
        ...form,
        timesPerWeek: form.type === "WEEKLY" ? form.timesPerWeek : null
      };
      let targetHabitId = id ?? habitIdForImage;
      if (id) {
        const updated = await apiRequest<HabitResponse>(`/api/habits/${id}`, { method: "PUT", body: JSON.stringify(payload) });
        setHabitIdForImage(updated.id);
        setHabitImageUrl(updated.imageUrl);
        targetHabitId = updated.id;
      } else {
        const created = await apiRequest<HabitResponse>("/api/habits", { method: "POST", body: JSON.stringify(payload) });
        setHabitIdForImage(created.id);
        setHabitImageUrl(created.imageUrl);
        targetHabitId = created.id;
      }

      if (pendingImageFile && targetHabitId) {
        setImageUploading(true);
        const updatedWithImage = await uploadHabitImage(targetHabitId, pendingImageFile);
        setHabitImageUrl(updatedWithImage.imageUrl);
        setPendingImageFile(null);
        if (pendingImagePreviewUrl) {
          URL.revokeObjectURL(pendingImagePreviewUrl);
          setPendingImagePreviewUrl(null);
        }
      }
      hapticImpact("heavy");
      navigate("/");
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSaving(false);
      setImageUploading(false);
    }
  };

  const onHabitImageSelected = async (file: File | null) => {
    if (!file) return;
    setError(null);

    if (!habitIdForImage) {
      setPendingImageFile(file);
      if (pendingImagePreviewUrl) {
        URL.revokeObjectURL(pendingImagePreviewUrl);
      }
      setPendingImagePreviewUrl(URL.createObjectURL(file));
      return;
    }

    setImageUploading(true);
    try {
      hapticImpact("light");
      const updated = await uploadHabitImage(habitIdForImage, file);
      setHabitImageUrl(updated.imageUrl);
      hapticImpact("medium");
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setImageUploading(false);
    }
  };

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">{id ? t("editHabit") : t("createHabit")}</h2>
        <p className="mt-1 text-xs text-slate-400">{t("habitSubtitle")}</p>
      </div>

      {error && <div className="glass-card p-3 text-sm text-rose-600">{error}</div>}

      <div className="glass-card space-y-4 p-4">
        <label className="block">
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">{t("title")}</span>
          <input
            className="w-full rounded-2xl bg-white/80 px-4 py-3 text-sm text-ink shadow-sm transition-all duration-200 focus:ring-2 focus:ring-sky-200"
            value={form.title}
            onChange={(e) => setForm((s) => ({ ...s, title: e.target.value }))}
            placeholder="Morning run"
          />
        </label>

        <div>
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">{t("type")}</span>
          <div className="relative grid grid-cols-2 rounded-2xl bg-white/70 p-1">
            <div
              className={`pointer-events-none absolute top-1 h-[calc(100%-8px)] w-[calc(50%-4px)] rounded-xl bg-ink shadow-sm transition-all duration-200 ${
                form.type === "WEEKLY" ? "translate-x-full" : "translate-x-0"
              }`}
            />
            {(["DAILY", "WEEKLY"] as HabitType[]).map((type) => (
              <button
                key={type}
                type="button"
                className={`tap relative z-10 rounded-xl px-4 py-2.5 text-sm font-bold transition-all duration-200 ${
                  form.type === type ? "text-white" : "text-slate-500"
                }`}
                onClick={() => {
                  hapticImpact("light");
                  setForm((s) => ({
                    ...s,
                    type,
                    timesPerWeek: type === "WEEKLY" ? (s.timesPerWeek ?? 1) : null
                  }));
                }}
              >
                {type === "DAILY" ? t("daily") : t("perWeek")}
              </button>
            ))}
          </div>
        </div>

        {form.type === "WEEKLY" && (
          <label className="block">
            <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">{t("timesPerWeek")}</span>
            <input
              type="number"
              min={1}
              max={7}
              className="w-full rounded-2xl bg-white/80 px-4 py-3 text-sm text-ink shadow-sm transition-all duration-200 focus:ring-2 focus:ring-sky-200"
              value={form.timesPerWeek ?? 1}
              onChange={(e) =>
                setForm((s) => {
                  const raw = Number(e.target.value);
                  const safe = Number.isFinite(raw) ? Math.min(7, Math.max(1, raw)) : 1;
                  return { ...s, timesPerWeek: safe };
                })
              }
            />
          </label>
        )}

        <div>
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">{t("color")}</span>
          <div className="flex gap-2">
            {colors.map((color) => (
              <button
                key={color}
                type="button"
                className={`tap h-9 w-9 rounded-full transition-all duration-200 ${
                  form.color === color ? "scale-105 shadow-md ring-1 ring-white" : "shadow-sm"
                }`}
                style={{
                  backgroundColor: color,
                  boxShadow: form.color === color ? `0 0 0 3px ${color}55` : undefined
                }}
                onClick={() => {
                  hapticImpact("light");
                  setForm((s) => ({ ...s, color }));
                }}
              />
            ))}
          </div>
        </div>

        <div>
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">{t("emoji")}</span>
          <div className="flex gap-2 overflow-x-auto pb-1">
            {emojis.map((emoji) => (
              <button
                key={emoji}
                type="button"
                className={`tap min-w-11 rounded-2xl bg-white/80 px-3 py-2 text-lg transition-all duration-200 ${
                  form.icon === emoji ? "scale-105 shadow-md ring-2 ring-ink/60" : ""
                }`}
                onClick={() => {
                  hapticImpact("light");
                  setForm((s) => ({ ...s, icon: emoji }));
                }}
              >
                {emoji}
              </button>
            ))}
          </div>
        </div>

        <div>
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">{t("habitPhoto")}</span>
          <div className="flex items-center gap-3 rounded-2xl bg-white/70 p-3">
            {habitImageUrl ? (
              <img src={resolveAssetUrl(habitImageUrl) ?? ""} alt={form.title || "Habit"} className="h-14 w-14 rounded-xl object-cover" />
            ) : pendingImagePreviewUrl ? (
              <img src={pendingImagePreviewUrl} alt={form.title || "Habit"} className="h-14 w-14 rounded-xl object-cover" />
            ) : (
              <div className="grid h-14 w-14 place-items-center rounded-xl bg-white text-2xl shadow-sm">{form.icon}</div>
            )}
            <label className="tap inline-flex cursor-pointer rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm">
              {imageUploading ? t("saving") : t("uploadHabitPhoto")}
              <input
                type="file"
                accept="image/png,image/jpeg,image/webp"
                className="hidden"
                onChange={(e) => void onHabitImageSelected(e.target.files?.[0] ?? null)}
                disabled={imageUploading}
              />
            </label>
          </div>
        </div>

        {id && (
          <label className="flex items-center gap-2 rounded-2xl bg-white/70 px-3 py-2 text-sm text-slate-600">
            <input
              type="checkbox"
              checked={form.archived}
              onChange={(e) => setForm((s) => ({ ...s, archived: e.target.checked }))}
            />
            {t("archiveHabit")}
          </label>
        )}
      </div>

      <div className="flex items-center justify-between">
        <button
          type="button"
          onClick={submit}
          disabled={saving}
          className="tap rounded-2xl bg-ink px-6 py-3 text-sm font-bold text-white shadow-md backdrop-blur transition-all duration-200 disabled:opacity-50"
        >
          {saving ? t("saving") : id ? t("save") : t("create")}
        </button>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="tap px-2 py-1 text-sm font-semibold text-slate-500 transition-all duration-200 hover:text-slate-700"
        >
          {t("cancel")}
        </button>
      </div>
    </section>
  );
}
