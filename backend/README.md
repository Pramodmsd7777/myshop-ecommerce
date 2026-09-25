# MyShop backend (Spring Boot)

Java 21, Spring Boot 3.3.5, MySQL, JWT auth, Razorpay payments.

## Run locally

1. Install Java 21, Maven, and have a MySQL server running.
2. Copy `.env.example` to `.env` (or just export the same variables in your shell / IDE run
   config) and fill in `DB_PASSWORD`. Everything else has a working local default.
3. `mvn spring-boot:run`
4. The API is at `http://localhost:8080/api`. `GET /api/health` should return `OK`.

On first run it creates the `ecommerce_db` database, all tables (`ddl-auto=update`), an admin
account (`ADMIN_EMAIL` / `ADMIN_PASSWORD`, default `admin@shop.com` / `ChangeMe@123`), and
(since `SEED_DEMO_DATA=true` by default) 10 categories and 40 affordably priced sample products
so the storefront isn't empty. Set `SEED_DEMO_DATA=false` to skip that.

**Change the admin password before deploying anywhere public.**

## Payments (Razorpay test mode)

Set `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` from your Razorpay dashboard in **Test Mode**
(API keys page). Test UPI: pay with `success@razorpay`, fail with `failure@razorpay`. See
`/docs/DEPLOYMENT.md` for the webhook setup, which needs a public URL.

## Tests / build

```
mvn -q -DskipTests package     # build the jar
mvn test                       # run tests (none included yet — add your own)
```

## Project layout

```
model/       JPA entities
repository/  Spring Data repositories + JPA Specifications
dto/         request/response records
security/    JWT filter, JwtService, UserDetailsService
service/     business logic (one class per feature)
controller/  REST endpoints
config/      SecurityConfig, RazorpayConfig, DataSeeder
exception/   GlobalExceptionHandler
```

See `/docs/API.md` for every endpoint and `/docs/DEPLOYMENT.md` for Railway/Render/Vercel.
