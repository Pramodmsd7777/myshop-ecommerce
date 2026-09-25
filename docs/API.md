# API reference

Base URL: `/api`. JSON in, JSON out. Protected endpoints need `Authorization: Bearer <token>`.

## Auth
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | /auth/register | none | `{name, email, password}` → `{token, id, name, email, role}`. Always creates role USER + an empty cart. |
| POST | /auth/login | none | `{email, password}` → same shape as register. |

## Categories & Products
| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | /categories | none | list all |
| POST | /categories | ADMIN | `{name, description}` |
| GET | /products | none | query params: `search, category, categoryId, minPrice, maxPrice, page, size, sort` (sort field must be `id`, `name`, or `price`) |
| GET | /products/{id} | none | |
| POST | /products | ADMIN | `{name, description, price, stock, imageUrl, categoryId}` |
| PUT | /products/{id} | ADMIN | same body |
| DELETE | /products/{id} | ADMIN | 409 if the product is referenced by a cart/order |

## Cart
| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | /cart | user | `{items[], totalItems, total}` |
| POST | /cart | user | `{productId, quantity}` — merges into an existing line, 409 if insufficient stock |
| PUT | /cart/{itemId} | user | `{quantity}` — itemId must belong to the caller |
| DELETE | /cart/{itemId} | user | |

## Orders
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | /orders | user | `{shippingAddress}` — checks out the current cart in one transaction: decrements stock, snapshots prices, clears the cart |
| GET | /orders | user | caller's orders, newest first |
| GET | /orders/{id} | user | 404 if it isn't the caller's order |

## Wishlist
| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | /wishlist | user | list of products |
| POST | /wishlist/{productId} | user | idempotent |
| DELETE | /wishlist/{productId} | user | idempotent |

## Payments (Razorpay, test mode)
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | /payments/create | user | `{orderId}` → `{keyId, razorpayOrderId, amount, currency, orderId}`; reuses an unfinished attempt |
| POST | /payments/verify | user | `{razorpayOrderId, razorpayPaymentId, razorpaySignature}` — server-side HMAC check, then marks the order PAID |
| POST | /payments/webhook | none (signature-authenticated) | Razorpay calls this directly; safety net if the browser tab closes mid-payment |

## Admin
| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | /admin/stats | ADMIN | `{totalUsers, totalProducts, totalOrders, totalRevenue, ordersByStatus}` |
| GET | /admin/orders | ADMIN | query: `status, page, size` |
| GET | /admin/orders/{id} | ADMIN | full detail incl. items and address |
| PATCH | /admin/orders/{id}/status | ADMIN | `{status}` — only allowed transitions succeed (PENDING→PAID/SHIPPED/CANCELLED, PAID→SHIPPED/CANCELLED, SHIPPED→DELIVERED); cancelling restores stock |
| GET | /admin/users | ADMIN | query: `search, page, size` — never returns password hashes |

## Error shape
Validation errors: `{"fieldName": "message", ...}` with `400`.
Everything else: `{"error": "message"}` with the matching status code (401/403/404/409/...).
