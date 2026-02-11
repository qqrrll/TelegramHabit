import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { clearToken, devAuth, getToken, setToken, telegramAuth } from "./api";

interface AuthContextValue {
  ready: boolean;
  isAuthenticated: boolean;
  error: string | null;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue>({
  ready: false,
  isAuthenticated: false,
  error: null,
  logout: () => {}
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(Boolean(getToken()));
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const telegram = window.Telegram?.WebApp;
    telegram?.ready();
    telegram?.expand();

    const existing = getToken();
    if (existing) {
      setIsAuthenticated(true);
      setReady(true);
      return;
    }

    const initData = telegram?.initData || import.meta.env.VITE_DEV_INIT_DATA || "";
    if (!initData) {
      const devAuthEnabled = String(import.meta.env.VITE_DEV_AUTH ?? "false") === "true";
      if (!devAuthEnabled) {
        setError("Run inside Telegram WebApp or enable VITE_DEV_AUTH.");
        setReady(true);
        return;
      }
      devAuth(999001, "Local User", "local_dev")
        .then((auth) => {
          setToken(auth.token);
          setIsAuthenticated(true);
        })
        .catch((e: Error) => setError(e.message))
        .finally(() => setReady(true));
      return;
    }

    telegramAuth(initData)
      .then((auth) => {
        setToken(auth.token);
        setIsAuthenticated(true);
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setReady(true));
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      ready,
      isAuthenticated,
      error,
      logout: () => {
        clearToken();
        setIsAuthenticated(false);
      }
    }),
    [ready, isAuthenticated, error]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  return useContext(AuthContext);
}
