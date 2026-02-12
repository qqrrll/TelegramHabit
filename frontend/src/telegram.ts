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

const HANDLED_START_PARAM_KEY = "handled_start_param";

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

export function isStartParamHandled(startParam: string): boolean {
  return sessionStorage.getItem(HANDLED_START_PARAM_KEY) === startParam;
}

export function markStartParamHandled(startParam: string): void {
  sessionStorage.setItem(HANDLED_START_PARAM_KEY, startParam);
}
