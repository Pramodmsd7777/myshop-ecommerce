# MyShop — Full-stack e-commerce (Spring Boot + React)

A 10-step build: auth, product catalog with search/filter, cart, orders, an admin
dashboard, wishlist, and Razorpay payments (test mode), with a dark, custom-themed
storefront and a rope-hanging login screen.

```
myshop/
├── backend/    Spring Boot 3.3.5, Java 21, MySQL, JWT, Razorpay
├── frontend/   React 19 + Vite, React Router, Axios
└── docs/       API reference, deployment guide, environment variables
```

## Quick start (local)

**Backend**
```
cd backend
cp .env.example .env      # fill in DB_PASSWORD; everything else has a working default
mvn spring-boot:run
```
Needs Java 21, Maven, and a running MySQL server. First run creates the database, all
tables, an admin account, and (by default) 10 categories with 40 affordable sample
products. See `backend/README.md`.

**Frontend**
```
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. The Vite dev server proxies `/api` to `http://localhost:8080`.

**Demo logins** (from the seed data): `admin@shop.com` / `ChangeMe@123` (admin),
or register your own customer account. **Change the admin password before deploying
anywhere public.**

## What's included

- JWT auth with USER/ADMIN roles, BCrypt passwords
- Product & category CRUD, search, category/price filters, sorting, pagination
- Cart with live stock checks; checkout that snapshots prices and decrements stock
  atomically inside one transaction (no overselling on concurrent checkouts)
- Order history + detail, admin order-status workflow (with stock restored on cancel)
- Admin dashboard: stats, product management, order management, user list
- Wishlist
- Razorpay payments in test mode: order creation, signature verification, and a
  webhook as the source of truth if the browser tab closes mid-payment
- Dark, gradient-accented theme with Poppins/Inter fonts, flip-card style product
  tiles, discount badges, and a swinging rope-hung login/register card

## Deploying

See `docs/DEPLOYMENT.md` for React → Vercel, Spring Boot → Railway or Render, and
MySQL/PostgreSQL setup, plus the Razorpay webhook configuration.

**Never commit real secrets.** `.env.example` files show every variable name; the
actual values belong in your host's environment variable settings, not in git.

## Notes and known limitations

- `spring.jpa.hibernate.ddl-auto=update` is fine to get started, but doesn't handle
  column type changes (e.g. widening an enum). Move to Flyway before changing the
  schema again.
- Orders never auto-expire, so an unpaid PENDING order holds its stock indefinitely.
  A scheduled job to cancel old PENDING orders (reusing the admin cancel logic) is a
  good next addition.
- Refunds for cancelled paid orders are manual (via the Razorpay dashboard) — no
  refund API call is wired up yet.
- No automated tests are included yet.
