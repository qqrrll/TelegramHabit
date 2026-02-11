import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import en from "./locales/en/translation.json";
import ru from "./locales/ru/translation.json";

const tgLang = (
  window as Window & { Telegram?: { WebApp?: { initDataUnsafe?: { user?: { language_code?: string } } } } }
).Telegram?.WebApp?.initDataUnsafe?.user?.language_code;
const browserLang = navigator.language?.slice(0, 2);
const initialLang = tgLang === "ru" || tgLang === "en" ? tgLang : browserLang === "ru" ? "ru" : "en";

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    ru: { translation: ru }
  },
  lng: initialLang,
  fallbackLng: "en",
  interpolation: { escapeValue: false }
});

export default i18n;
