import { Link, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAuth } from "./auth";
import { ActivityPage } from "./pages/ActivityPage";
import { FriendsPage } from "./pages/FriendsPage";
import { FriendHabitStatsPage } from "./pages/FriendHabitStatsPage";
import { FriendProfilePage } from "./pages/FriendProfilePage";
import { HabitFormPage } from "./pages/HabitFormPage";
import { HabitStatsPage } from "./pages/HabitStatsPage";
import { HomePage } from "./pages/HomePage";
import { ProfilePage } from "./pages/ProfilePage";
import { SkeletonCard } from "./components/Skeleton";
import { isStartParamHandled, readStartParam } from "./telegram";

function StartParamRouter() {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const startParam = readStartParam();
    if (!startParam?.startsWith("friend_")) return;
    if (isStartParamHandled(startParam)) return;
    if (location.pathname === "/friends") return;
    navigate(`/friends?code=${encodeURIComponent(startParam.slice("friend_".length))}`, { replace: true });
  }, [location.pathname, navigate]);

  return null;
}

function Navigation() {
  const location = useLocation();
  const { t } = useTranslation();
  const itemClass = (active: boolean) =>
    `tap flex-1 rounded-2xl px-2 py-2 text-center text-xs font-semibold transition-all duration-200 ${
      active ? "bg-white/90 text-ink" : "text-slate-500"
    }`;

  return (
    <nav className="glass-card sticky top-3 z-20 flex items-center gap-1.5 border-white/60 p-1.5 shadow-sm">
      <Link className={itemClass(location.pathname === "/")} to="/">
        {t("today")}
      </Link>
      <Link className={itemClass(location.pathname.startsWith("/activity"))} to="/activity">
        {t("feed")}
      </Link>
      <Link className={itemClass(location.pathname.startsWith("/habits/new"))} to="/habits/new">
        {t("create")}
      </Link>
      <Link className={itemClass(location.pathname.startsWith("/friends"))} to="/friends">
        {t("friends")}
      </Link>
      <Link className={itemClass(location.pathname.startsWith("/profile"))} to="/profile">
        {t("profile")}
      </Link>
    </nav>
  );
}

function FullState({ text, error = false }: { text: string; error?: boolean }) {
  return (
    <div className="mx-auto flex min-h-screen max-w-md items-center justify-center px-4">
      {error ? (
        <div className="glass-card w-full px-6 py-8 text-center text-sm text-rose-600">{text}</div>
      ) : (
        <div className="w-full space-y-3">
          <SkeletonCard />
          <p className="text-center text-sm text-slate-500">{text}</p>
        </div>
      )}
    </div>
  );
}

export default function App() {
  const { t } = useTranslation();
  const { ready, isAuthenticated, error } = useAuth();

  if (!ready) return <FullState text={t("loadingSession")} />;
  if (error) return <FullState text={error} error />;
  if (!isAuthenticated) return <FullState text={t("authFailed")} error />;

  return (
    <div className="mx-auto min-h-screen max-w-md px-4 pb-8 pt-4">
      <header className="mb-3 flex items-center justify-between">
        <h1 className="text-3xl font-black text-ink">{t("appTitle")}</h1>
        <div className="rounded-full bg-ink px-3 py-1 text-[11px] font-bold text-white shadow-sm">{t("pro")}</div>
      </header>

      <Navigation />
      <StartParamRouter />

      <main className="mt-4">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/activity" element={<ActivityPage />} />
          <Route path="/habits/new" element={<HabitFormPage />} />
          <Route path="/habits/:id/edit" element={<HabitFormPage />} />
          <Route path="/habits/:id/stats" element={<HabitStatsPage />} />
          <Route path="/friends" element={<FriendsPage />} />
          <Route path="/friends/:friendId" element={<FriendProfilePage />} />
          <Route path="/friends/:friendId/habits/:habitId/stats" element={<FriendHabitStatsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
