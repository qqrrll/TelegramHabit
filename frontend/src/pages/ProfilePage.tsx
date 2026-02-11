import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { getMyProfile, resolveAssetUrl, updateMyLanguage, uploadMyAvatar } from "../api";
import { hapticImpact } from "../telegram";
import { SkeletonProfile } from "../components/Skeleton";
import type { UserProfileResponse } from "../types";

export function ProfilePage() {
  const { t, i18n } = useTranslation();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [lang, setLang] = useState<"ru" | "en">("en");
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [savingLang, setSavingLang] = useState(false);
  const [flashAvatar, setFlashAvatar] = useState(false);
  const [flashLanguage, setFlashLanguage] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    getMyProfile()
      .then((data) => {
        setProfile(data);
        setLang(data.language ?? "en");
        i18n.changeLanguage(data.language ?? "en").catch(() => undefined);
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [i18n]);

  const onPickAvatar = () => inputRef.current?.click();

  const onAvatarSelected = async (file: File | null) => {
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      hapticImpact("medium");
      const updated = await uploadMyAvatar(file);
      setProfile(updated);
      setFlashAvatar(true);
      window.setTimeout(() => setFlashAvatar(false), 320);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setUploading(false);
    }
  };

  const onChangeLanguage = async (next: "ru" | "en") => {
    if (next === lang) return;
    setLang(next);
    setSavingLang(true);
    setError(null);
    try {
      hapticImpact("light");
      const updated = await updateMyLanguage(next);
      setProfile(updated);
      await i18n.changeLanguage(updated.language);
      hapticImpact("medium");
      setFlashLanguage(true);
      window.setTimeout(() => setFlashLanguage(false), 320);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSavingLang(false);
    }
  };

  if (loading) return <SkeletonProfile />;
  if (!profile) return <p className="px-1 py-2 text-sm text-rose-600">{error ?? "Profile unavailable"}</p>;

  const displayName = [profile.firstName, profile.lastName].filter(Boolean).join(" ") || profile.username || "User";
  const avatarSrc = resolveAssetUrl(profile.photoUrl);

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">{t("profileTitle")}</h2>
        <p className="mt-1 text-xs text-slate-400">{t("profileSubtitle")}</p>
      </div>

      <article className={`glass-card p-4 transition-all duration-200 ${flashAvatar ? "ring-2 ring-emerald-300" : ""}`}>
        <div className="flex flex-col items-center gap-3 text-center">
          <button
            type="button"
            onClick={onPickAvatar}
            className="tap relative h-24 w-24 rounded-full bg-white shadow-md transition-all duration-200"
            disabled={uploading}
          >
            {avatarSrc ? (
              <img src={avatarSrc} alt={displayName} className="h-full w-full rounded-full object-cover" />
            ) : (
              <span className="grid h-full w-full place-items-center text-3xl">👤</span>
            )}
            {uploading && (
              <span className="absolute inset-0 grid place-items-center rounded-full bg-black/40 text-xs font-bold text-white">
                {t("saving")}
              </span>
            )}
          </button>
          <input
            ref={inputRef}
            type="file"
            accept="image/png,image/jpeg,image/webp"
            className="hidden"
            onChange={(e) => void onAvatarSelected(e.target.files?.[0] ?? null)}
          />
          <h3 className="text-base font-black text-ink">{displayName}</h3>
          <p className="text-xs text-slate-400">@{profile.username ?? "unknown"}</p>
          <button
            type="button"
            onClick={onPickAvatar}
            className="tap rounded-full bg-white/80 px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm transition-all duration-200"
          >
            {t("uploadPhoto")}
          </button>
        </div>
      </article>

      <article className={`glass-card space-y-3 p-4 transition-all duration-200 ${flashLanguage ? "ring-2 ring-emerald-300" : ""}`}>
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">{t("language")}</p>
        <div className="grid grid-cols-2 gap-2">
          <button
            type="button"
            className={`tap rounded-2xl px-4 py-3 text-sm font-bold transition-all duration-200 ${
              lang === "ru" ? "bg-ink text-white shadow-md" : "bg-white/80 text-slate-500"
            }`}
            onClick={() => void onChangeLanguage("ru")}
          >
            {t("russian")}
          </button>
          <button
            type="button"
            className={`tap rounded-2xl px-4 py-3 text-sm font-bold transition-all duration-200 ${
              lang === "en" ? "bg-ink text-white shadow-md" : "bg-white/80 text-slate-500"
            }`}
            onClick={() => void onChangeLanguage("en")}
          >
            {t("english")}
          </button>
        </div>
        {savingLang && <p className="text-xs text-slate-400">{t("saving")}</p>}
        {error && <p className="text-sm text-rose-600">{error}</p>}
      </article>
    </section>
  );
}
