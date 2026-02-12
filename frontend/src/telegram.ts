function webApp() {
  return (window as Window & {
    Telegram?: {
      WebApp?: {
        ready?: () => void;
        expand?: () => void;
        initDataUnsafe?: { start_param?: string };
        HapticFeedback?: { impactOccurred: (style: "light" | "medium" | "heavy") => void };
      };
    };
  }).Telegram?.WebApp;
}

export function initTelegramApp(): void {
  try {
    webApp()?.ready?.();
    webApp()?.expand?.();
  } catch {
    // no-op in browser/dev mode
  }
}

export function hapticImpact(style: "light" | "medium" | "heavy" = "light"): void {
  try {
    webApp()?.HapticFeedback?.impactOccurred(style);
  } catch {
    // no-op in browser/dev mode
  }
}

export function readStartParam(): string | null {
  const fromInitData = webApp()?.initDataUnsafe?.start_param?.trim();
  if (fromInitData) return fromInitData;
  const fromQuery = new URLSearchParams(window.location.search).get("tgWebAppStartParam")?.trim();
  if (fromQuery) return fromQuery;
  return null;
}
