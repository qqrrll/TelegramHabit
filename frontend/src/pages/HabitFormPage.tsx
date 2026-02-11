import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { apiRequest } from "../api";
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
const emojis = ["🔥", "💪", "📚", "🏃", "💧", "🧘", "🎯", "🥗"];

export function HabitFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState<HabitRequest>(defaultForm);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

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
      })
      .catch((e: Error) => setError(e.message));
  }, [id]);

  const submit = async () => {
    setSaving(true);
    setError(null);
    try {
      const payload: HabitRequest = {
        ...form,
        timesPerWeek: form.type === "WEEKLY" ? form.timesPerWeek : null
      };
      if (id) {
        await apiRequest(`/api/habits/${id}`, { method: "PUT", body: JSON.stringify(payload) });
      } else {
        await apiRequest("/api/habits", { method: "POST", body: JSON.stringify(payload) });
      }
      navigate("/");
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">{id ? "Edit habit" : "Create habit"}</h2>
        <p className="mt-1 text-xs text-slate-400">Design your daily ritual like a premium fitness app.</p>
      </div>

      {error && <div className="glass-card p-3 text-sm text-rose-600">{error}</div>}

      <div className="glass-card space-y-4 p-4">
        <label className="block">
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">Title</span>
          <input
            className="w-full rounded-2xl bg-white/80 px-4 py-3 text-sm text-ink shadow-sm transition-all duration-200 focus:ring-2 focus:ring-sky-200"
            value={form.title}
            onChange={(e) => setForm((s) => ({ ...s, title: e.target.value }))}
            placeholder="Morning run"
          />
        </label>

        <div>
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">Type</span>
          <div className="grid grid-cols-2 gap-2">
            {(["DAILY", "WEEKLY"] as HabitType[]).map((type) => (
              <button
                key={type}
                type="button"
                className={`tap rounded-2xl px-4 py-3 text-sm font-bold transition-all duration-200 ${
                  form.type === type ? "bg-ink text-white shadow-md" : "bg-white/80 text-slate-500"
                }`}
                onClick={() =>
                  setForm((s) => ({
                    ...s,
                    type,
                    timesPerWeek: type === "WEEKLY" ? (s.timesPerWeek ?? 1) : null
                  }))
                }
              >
                {type === "DAILY" ? "Daily" : "N / week"}
              </button>
            ))}
          </div>
        </div>

        {form.type === "WEEKLY" && (
          <label className="block">
            <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">Times per week</span>
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
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">Color</span>
          <div className="flex gap-2">
            {colors.map((color) => (
              <button
                key={color}
                type="button"
                className={`tap h-9 w-9 rounded-full shadow-md transition-all duration-200 ${
                  form.color === color ? "ring-2 ring-ink ring-offset-2" : ""
                }`}
                style={{ backgroundColor: color }}
                onClick={() => setForm((s) => ({ ...s, color }))}
              />
            ))}
          </div>
        </div>

        <div>
          <span className="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">Emoji</span>
          <div className="grid grid-cols-8 gap-2">
            {emojis.map((emoji) => (
              <button
                key={emoji}
                type="button"
                className={`tap rounded-2xl bg-white/80 py-2 text-lg transition-all duration-200 ${
                  form.icon === emoji ? "shadow-md ring-2 ring-ink/70" : ""
                }`}
                onClick={() => setForm((s) => ({ ...s, icon: emoji }))}
              >
                {emoji}
              </button>
            ))}
          </div>
        </div>

        <label className="flex items-center gap-2 rounded-2xl bg-white/70 px-3 py-2 text-sm text-slate-600">
          <input
            type="checkbox"
            checked={form.archived}
            onChange={(e) => setForm((s) => ({ ...s, archived: e.target.checked }))}
          />
          Archive habit
        </label>
      </div>

      <div className="grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={submit}
          disabled={saving}
          className="tap rounded-2xl bg-ink px-4 py-3 text-sm font-bold text-white shadow-md backdrop-blur transition-all duration-200 disabled:opacity-50"
        >
          {saving ? "Saving..." : id ? "Save" : "Create"}
        </button>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="tap rounded-2xl bg-white/80 px-4 py-3 text-sm font-bold text-slate-600 shadow-md backdrop-blur transition-all duration-200"
        >
          Cancel
        </button>
      </div>
    </section>
  );
}
