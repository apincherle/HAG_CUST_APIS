# Shopify webhooks — local development

> **Full guide:** [testing_shopify_webhooks_locally.md](./testing_shopify_webhooks_locally.md) — fixtures, `mvn test`, PowerShell scripts, ngrok, troubleshooting.

Run the stack with Docker Compose (PostgreSQL + API). Webhook secrets and shop domain are set in `docker-compose.yml`.

## Start

```bash
docker compose up --build
```

API: `http://localhost:8001`  
Webhook URL (local only): `http://localhost:8001/api/webhooks/shopify`

Shopify requires **HTTPS** for real store webhooks. For local testing use either:

1. **curl** against localhost (no Shopify delivery), or  
2. **ngrok** / Cloudflare Tunnel: `ngrok http 8001` → register `https://<tunnel>/api/webhooks/shopify` in Shopify Admin.

## Docker Compose secrets (dev)

| Variable | Default in compose |
|----------|-------------------|
| `SHOPIFY_WEBHOOK_SECRET` | `dev-webhook-secret-change-me` |
| `SHOPIFY_SHOP_DOMAIN` | `h-a-g-s.myshopify.com` (set in compose for HAGS store) |
| `SHOPIFY_WEBHOOK_VERIFY_HMAC` | `true` |

`X-Shopify-Shop-Domain` on each request must match `SHOPIFY_SHOP_DOMAIN` when that variable is set.

## Test customer webhook (PowerShell)

```powershell
$secret = "dev-webhook-secret-change-me"
$body = '{"id":9001,"email":"shopify-test@example.com","first_name":"Shop","last_name":"Test"}'
$hmac = [Convert]::ToBase64String(
  (New-Object System.Security.Cryptography.HMACSHA256(
    , [Text.Encoding]::UTF8.GetBytes($secret)
  ).ComputeHash([Text.Encoding]::UTF8.GetBytes($body)))
)

Invoke-WebRequest -Uri "http://localhost:8001/api/webhooks/shopify" -Method POST `
  -Body $body -ContentType "application/json" `
  -Headers @{
    "X-Shopify-Topic" = "customers/create"
    "X-Shopify-Shop-Domain" = "dev-store.myshopify.com"
    "X-Shopify-Webhook-Id" = "test-webhook-001"
    "X-Shopify-Hmac-Sha256" = $hmac
  }
```

## Test order paid (creates entitlement)

Use SKU `HAGS-SUB-BRONZE` (mapped to tier BRONZE, 10 cards) in `application.properties`.

```powershell
$secret = "dev-webhook-secret-change-me"
$body = '{"id":5001,"name":"#1001","financial_status":"paid","customer":{"id":9001},"line_items":[{"id":6001,"sku":"HAGS-SUB-BRONZE","quantity":1}]}'
# ... same HMAC calculation as above ...
# X-Shopify-Topic = orders/paid
# X-Shopify-Webhook-Id = test-webhook-002
```

## Disable HMAC (quick manual tests only)

Set in `docker-compose.yml`:

```yaml
- SHOPIFY_WEBHOOK_VERIFY_HMAC=false
```

Do not use in production.

## Verify in database

```bash
docker exec -it hags-postgres psql -U hags_user -d hags_customer -c "SELECT shopify_customer_id, email, full_name FROM customers WHERE shopify_customer_id IS NOT NULL;"
docker exec -it hags-postgres psql -U hags_user -d hags_customer -c "SELECT * FROM purchase_entitlements;"
docker exec -it hags-postgres psql -U hags_user -d hags_customer -c "SELECT webhook_id, topic, status FROM shopify_webhook_events;"
```

## Supported topics

| Topic | Behaviour |
|-------|-----------|
| `customers/create`, `customers/update` | Upsert `customers` by `shopify_customer_id` |
| `orders/paid` | Create `purchase_entitlements` for mapped SKUs |
| `orders/cancelled` | Mark entitlements cancelled |
| `orders/create`, `orders/updated` | Logged only (MVP) |

See [shopify_webhook_integration_plan.md](./shopify_webhook_integration_plan.md) for full architecture.
