interface TelegramWebApp {
  initData: string;
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
