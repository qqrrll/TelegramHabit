import { Link, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { useAuth } from "./auth";
import { ActivityPage } from "./pages/ActivityPage";
import { HabitFormPage } from "./pages/HabitFormPage";
import { HabitStatsPage } from "./pages/HabitStatsPage";
import { HomePage } from "./pages/HomePage";

function Navigation() {
  const location = useLocation();
  const itemClass = (active: boolean) =>
    `tap flex-1 rounded-2xl px-4 py-2.5 text-center text-sm font-semibold transition-all duration-200 ${
      active ? "bg-white text-ink shadow-md" : "text-slate-500"
    }`;

  return (
    <nav className="glass-card sticky top-3 z-20 flex items-center gap-2 p-2">
      <Link className={itemClass(location.pathname === "/")} to="/">
        Today
      </Link>
      <Link className={itemClass(location.pathname.startsWith("/activity"))} to="/activity">
        Feed
      </Link>
      <Link className={itemClass(location.pathname.startsWith("/habits/new"))} to="/habits/new">
        Create
      </Link>
    </nav>
  );
}

function FullState({ text, error = false }: { text: string; error?: boolean }) {
  return (
    <div className="mx-auto flex min-h-screen max-w-md items-center justify-center px-4">
      <div className={`glass-card w-full px-6 py-8 text-center text-sm ${error ? "text-rose-600" : "text-slate-500"}`}>
        {text}
      </div>
    </div>
  );
}

export default function App() {
  const { ready, isAuthenticated, error } = useAuth();

  if (!ready) return <FullState text="Preparing your mini app..." />;
  if (error) return <FullState text={error} error />;
  if (!isAuthenticated) return <FullState text="Authentication failed" error />;

  return (
    <div className="mx-auto min-h-screen max-w-md px-4 pb-8 pt-4">
      <header className="mb-3 flex items-center justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">Telegram Mini App</p>
          <h1 className="text-2xl font-black text-ink">Habit Tracker</h1>
        </div>
        <div className="glass-card rounded-2xl px-3 py-2 text-xs font-semibold text-slate-500">Premium</div>
      </header>

      <Navigation />

      <main className="mt-4">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/activity" element={<ActivityPage />} />
          <Route path="/habits/new" element={<HabitFormPage />} />
          <Route path="/habits/:id/edit" element={<HabitFormPage />} />
          <Route path="/habits/:id/stats" element={<HabitStatsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
