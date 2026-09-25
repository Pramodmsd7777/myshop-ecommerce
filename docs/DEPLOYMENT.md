# Deployment guide

React → Vercel · Spring Boot → Railway or Render · MySQL or PostgreSQL

## 1. Prepare the code

- All secrets are read from environment variables (see `backend/.env.example` and
  `frontend/.env.example`). Nothing sensitive is hard-coded.
- `backend/src/main/resources/application-prod.properties` has **no defaults** for
  secrets, so the app refuses to start if one is missing in production — safer than
  silently running with an empty JWT secret.
- `backend/Dockerfile` builds and runs the jar with `-Dspring.profiles.active=prod`.
- `frontend/vercel.json` rewrites all paths to `index.html` so client-side routes
  (e.g. `/products/5`) don't 404 on refresh.

## 2. Choose a backend host + database

| | Railway | Render |
|---|---|---|
| Databases | MySQL and PostgreSQL | Managed Postgres (no managed MySQL) |
| Free tier | One-time trial credit, then a small paid plan | Free web service (spins down after 15 min idle) + free Postgres (expires after 30 days) |
| Best for | Keeping MySQL as-is, always-on | A free demo on Postgres |

Recommendation for a portfolio project: **Railway + MySQL** (~$5/month after the
trial), since it's the smallest change from local development and has no cold starts.
To use Postgres instead, swap `mysql-connector-j` for `org.postgresql:postgresql` in
`pom.xml` and use a `jdbc:postgresql://host:5432/db` URL — the rest of the code (JPQL
queries, locks) works unchanged on either database.

## 3. Deploy the backend

**Railway**
1. New project → "Deploy from GitHub repo" → set the root directory to `backend` if
   the repo contains both folders. Railway builds the Dockerfile automatically.
2. Add a MySQL database in the same project.
3. Set environment variables on the backend service: `DB_URL`, `DB_USER`,
   `DB_PASSWORD` (from the MySQL service's own variables — check its exact variable
   names on its Variables tab), `JWT_SECRET` (`openssl rand -base64 32`),
   `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `CORS_ORIGINS` (fill in after step 4), and the
   three `RAZORPAY_*` values.
4. Generate a public domain under the service's networking settings. Confirm
   `https://<domain>/api/health` returns `OK`.

**Render**
1. Create a PostgreSQL database (or use an external one).
2. New Web Service from the repo → Docker → root directory `backend` → health check
   path `/api/health`.
3. Same environment variables. Build `DB_URL` in JDBC form yourself:
   `jdbc:postgresql://<host>:5432/<database>` — Render's own connection string isn't
   in that form.

Either way, first startup creates the tables, the admin account, and (if
`SEED_DEMO_DATA=true`) the sample catalog. Set `SEED_DEMO_DATA=false` once you have
your own data.

## 4. Deploy the frontend to Vercel

1. Import the repo, set the root directory to `frontend`. Framework preset: Vite.
2. Environment variable: `VITE_API_URL` = your backend's origin (no trailing slash,
   no `/api`), e.g. `https://myshop-api.up.railway.app`.
3. Deploy, then copy the resulting Vercel URL into the backend's `CORS_ORIGINS` and
   restart/redeploy the backend. `VITE_*` variables are baked in at build time and
   are public — never put secrets there.

## 5. Razorpay test mode

1. Create a Razorpay account, switch the dashboard to **Test Mode**, and copy the
   test API keys (`rzp_test_...`) into `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET`.
2. Test payment: UPI id `success@razorpay` succeeds, `failure@razorpay` fails; test
   cards accept any future expiry and any CVV.
3. Webhook (needed for the "closed the tab before it finished" case): in the
   dashboard's Webhooks page, set the URL to
   `https://<your-backend-domain>/api/payments/webhook`, subscribe to `order.paid`,
   and put the same secret in `RAZORPAY_WEBHOOK_SECRET`. This can't be tested against
   plain `localhost` — it needs the deployed URL (or a tunnel like ngrok locally).

## 6. Before sharing the link

- Rotate any secret that was ever committed to git, even briefly.
- Change the seeded admin password from the `.env.example` default.
- Live Razorpay keys need a verified business account — keep test keys for a demo.
- Free-tier databases (Render) aren't backups; export important data if it matters.
