export function hapticImpact(style: "light" | "medium" | "heavy" = "light"): void {
  try {
    (window as Window & { Telegram?: { WebApp?: { HapticFeedback?: { impactOccurred: (s: "light" | "medium" | "heavy") => void } } } })
      .Telegram?.WebApp?.HapticFeedback?.impactOccurred(style);
  } catch {
    // no-op in browser/dev mode
  }
}
