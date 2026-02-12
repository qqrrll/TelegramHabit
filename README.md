# Telegram Habit Tracker Mini App

## 1. Configure environment

Backend (`src/main/resources/application.yml` reads these vars):

```bash
DB_URL=jdbc:postgresql://localhost:5432/telegramHabit
DB_USER=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_long_secret
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_USERNAME=habit_tracker_miniapp_bot
TELEGRAM_MINIAPP_SHORT_NAME=app
CORS_ALLOWED_ORIGINS=https://your-frontend-domain
FRIEND_INVITE_BASE_URL=https://your-frontend-domain/friends
```

Frontend (`frontend/.env`):

```bash
VITE_API_BASE_URL=https://your-backend-domain
VITE_DEV_AUTH=false
```

## 2. Build and run

```bash
# backend
./mvnw spring-boot:run

# frontend
cd frontend
npm install
npm run build
```

## 3. BotFather setup for Mini App

Run these commands in `@BotFather`:

1. `/mybots` -> select your bot.
2. `Bot Settings` -> `Menu Button` -> set Web App URL (your frontend domain).
3. `Bot Settings` -> `Domain` -> set the same frontend domain.
4. `Configure Mini App` -> set short name (example: `app`).

Then open mini app from bot menu.

## 4. Friend invite flow in Telegram

- Backend generates Telegram deep-link:
  - `https://t.me/<bot_username>/<miniapp_short_name>?startapp=friend_<code>`
- Frontend reads `startapp` and auto-opens `/friends`, then accepts invite.
- If bot fields are empty, backend falls back to:
  - `FRIEND_INVITE_BASE_URL?code=<code>`

