interface TelegramWebApp {
  initData: string;
  initDataUnsafe?: {
    user?: {
      language_code?: string;
    };
  };
  HapticFeedback?: {
    impactOccurred(style: "light" | "medium" | "heavy"): void;
  };
  ready(): void;
  expand(): void;
}

interface TelegramObject {
  WebApp: TelegramWebApp;
}

declare global {
  interface Window {
    Telegram?: TelegramObject;
  }
}

export {};
