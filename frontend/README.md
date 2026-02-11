# Telegram Habit Frontend

## Local run

1. Install dependencies:
`npm install`
2. Run dev server:
`npm run dev`
3. Set backend URL if needed:
`VITE_API_BASE_URL=http://localhost:8080`
4. For local auth without Telegram:
`VITE_DEV_AUTH=true`

Optional for browser testing outside Telegram:
`VITE_DEV_INIT_DATA=<copied initData>`

## Backend local auth mode (without Telegram)

Enable in backend env:
`DEV_AUTH_ENABLED=true`

Then frontend can authenticate through `/api/auth/dev` automatically in local mode.

## Telegram Mini App setup

1. Create bot in `@BotFather` with `/newbot`.
2. Save token and set in backend env:
`TELEGRAM_BOT_TOKEN=...`
3. Deploy frontend to public `https` domain.
4. In `@BotFather` set Mini App URL:
- `/setdomain` -> your frontend domain
- `/setmenubutton` -> `Web App` + frontend URL
5. In backend set CORS:
`CORS_ALLOWED_ORIGINS=https://your-frontend-domain`

After this, open bot menu button and Telegram will launch this frontend as WebApp.
