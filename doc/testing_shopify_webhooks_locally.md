# Testing Shopify webhooks locally (HAGS)

This guide covers **automated tests** (fixtures + JUnit) and **manual tests** (Docker + curl/PowerShell) against your HAGS API.

**Endpoint (always the same path):**

```text
POST http://localhost:8001/api/webhooks/shopify
```

**Your Shopify store:** `h-a-g-s.myshopify.com`

---

## 1. Automated tests (recommended first step)

Tests use real JSON fixtures shaped like Shopify payloads and an in-memory H2 database (`test` profile).

### Fixtures

| File | Topic | What it represents |
|------|--------|-------------------|
| `src/test/resources/com/example/repository/cusomers/update.json` | `customers/create`, `customers/update` | HAGS collector **Alex Collector**, Shopify customer `8800123456789` |
| `src/test/resources/orders/create.json` | `orders/create` | Order **#HAGS-1001**, status **pending**, SKU `HAGS-SUB-BRONZE` |
| `src/test/resources/orders/paid.json` | `orders/paid` | Same order, status **paid** → creates entitlement (10 cards, BRONZE) |

Shared IDs across fixtures:

| Field | Value |
|--------|--------|
| Shopify customer id | `8800123456789` |
| Shopify order id | `55000112233` |
| Line item id | `77000112233` |
| Email | `collector@hags-grading.co.uk` |

### Run tests

```bash
mvn test -Dtest=ShopifyWebhookIntegrationTest
```

Or inside Docker (builds with Lombok correctly):

```bash
docker run --rm -v "%cd%":/app -w /app eclipse-temurin:17-jdk-alpine sh -c "apk add --no-cache maven && mvn -q test -Dtest=ShopifyWebhookIntegrationTest"
```

**What the tests assert**

1. `customers/update` → customer row with Shopify id, email, name, London address  
2. `customers/create` → same payload shape still upserts  
3. `orders/create` → HTTP 200, **no** `purchase_entitlements` row  
4. `orders/paid` → entitlement for `HAGS-SUB-BRONZE`, tier `BRONZE`, 10 cards  
5. Duplicate `X-Shopify-Webhook-Id` → 200, no double entitlement  
6. Bad HMAC → 401  

Test HMAC secret: `test-webhook-secret` (see `src/test/resources/application-test.properties`).

---

## 2. Manual tests with Docker Compose

### Start stack

```bash
docker compose up --build
```

### Align secrets with Shopify Admin

In **Settings → Notifications → Webhooks**, Shopify shows a signing secret. Set in `docker-compose.yml`:

```yaml
- SHOPIFY_WEBHOOK_SECRET=<your-signing-secret-from-shopify>
- SHOPIFY_SHOP_DOMAIN=h-a-g-s.myshopify.com
```

For quick local-only runs without matching Shopify, you can keep the default `dev-webhook-secret-change-me` and sign requests yourself (below).

Restart after changes:

```bash
docker compose up --build
```

### Post fixtures with PowerShell

From the repo root:

```powershell
$secret = "dev-webhook-secret-change-me"   # or your Shopify signing secret
$shop   = "h-a-g-s.myshopify.com"

function Send-HagsWebhook {
  param(
    [string]$Topic,
    [string]$FixturePath,
    [string]$WebhookId
  )
  $body = [System.IO.File]::ReadAllBytes((Resolve-Path $FixturePath))
  $hmac = [Convert]::ToBase64String(
    (New-Object System.Security.Cryptography.HMACSHA256(
      , [Text.Encoding]::UTF8.GetBytes($secret)
    ).ComputeHash($body))
  )
  Invoke-WebRequest -Uri "http://localhost:8001/api/webhooks/shopify" -Method POST `
    -Body $body -ContentType "application/json; charset=utf-8" `
    -Headers @{
      "X-Shopify-Topic"       = $Topic
      "X-Shopify-Shop-Domain" = $shop
      "X-Shopify-Webhook-Id"  = $WebhookId
      "X-Shopify-Hmac-Sha256" = $hmac
    }
}

# 1) Customer
Send-HagsWebhook -Topic "customers/update" `
  -FixturePath "src\test\resources\com\example\repository\cusomers\update.json" `
  -WebhookId "manual-customer-001"

# 2) Order placed (logged only)
Send-HagsWebhook -Topic "orders/create" `
  -FixturePath "src\test\resources\orders\create.json" `
  -WebhookId "manual-order-create-001"

# 3) Order paid (creates entitlement)
Send-HagsWebhook -Topic "orders/paid" `
  -FixturePath "src\test\resources\orders\paid.json" `
  -WebhookId "manual-order-paid-001"
```

### Verify in PostgreSQL

```bash
docker exec -it hags-postgres psql -U hags_user -d hags_customer -c ^
  "SELECT shopify_customer_id, email, full_name FROM customers WHERE shopify_customer_id = 8800123456789;"

docker exec -it hags-postgres psql -U hags_user -d hags_customer -c ^
  "SELECT shopify_order_id, tier_code, cards_allowed, status FROM purchase_entitlements WHERE shopify_order_id = 55000112233;"

docker exec -it hags-postgres psql -U hags_user -d hags_customer -c ^
  "SELECT webhook_id, topic, status FROM shopify_webhook_events ORDER BY received_at DESC LIMIT 10;"
```

---

## 3. Receiving webhooks from real Shopify (not localhost)

Shopify **cannot** POST to `http://localhost:8001`. Use either:

| Approach | Webhook URL in Shopify Admin |
|----------|------------------------------|
| **ngrok** (local API running) | `https://<subdomain>.ngrok-free.app/api/webhooks/shopify` |
| **Deployed API** | `https://api.<your-domain>/api/webhooks/shopify` |

```bash
ngrok http 8001
```

Register the **same path** for each event (Customer creation, Customer update, Order payment, etc.).

Signing secret in Docker **must** match the value shown in Shopify Admin.

---

## 4. Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| **401** Invalid signature | `SHOPIFY_WEBHOOK_SECRET` ≠ Shopify signing secret, or body changed after signing |
| **401** Unexpected shop domain | `X-Shopify-Shop-Domain` ≠ `SHOPIFY_SHOP_DOMAIN` (use `h-a-g-s.myshopify.com`). Set `SHOPIFY_WEBHOOK_LOG_PAYLOAD=true` and check Container App logs for `Shopify webhook received:` / `Shopify webhook JSON body:` |
| **200** but no entitlement | Wrong topic (`orders/create` does not create entitlements; use `orders/paid`) or SKU not in `shopify.tier-sku-mapping.*` |
| **500** on order paid | Customer missing (run customer webhook first) or DB constraint — check app logs |
| Shopify never hits API | URL not HTTPS/public; use ngrok or deploy |

### Optional: disable HMAC (manual only)

In `docker-compose.yml`:

```yaml
- SHOPIFY_WEBHOOK_VERIFY_HMAC=false
```

Never use in production.

---

## 5. Related docs

- [shopify_webhook_integration_plan.md](./shopify_webhook_integration_plan.md) — architecture and done / not done  
- [shopify_local_development.md](./shopify_local_development.md) — short Docker reference  
