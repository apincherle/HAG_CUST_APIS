# HAG platform — delivery todo

Ordered by dependency. **(this repo)** = `HAG_CUST_APIS`; other items reference sibling services from project docs (`HAGS_ximilar_ai`, `hags_certificate_lookup_ui`, etc.).

Related docs:

- [Azure SQL connection reference](./azure_sql_connection.md) — server, database, Key Vault values
- [Azure variables checklist](./azure_variables_and_config_checklist.md)
- [Azure DevOps pipeline setup](./azure_devops_pipeline_setup.md)
- [Shopify webhook plan](./shopify_webhook_integration_plan.md)
- [Certificate QR flow](./certificate_qr_lookup_flow.md)

---

## Phase 1 — Azure foundation & data

### Azure SQL (prod)

- [ ] Provision Azure SQL server + database (see [detailed setup below](#azure-sql-detailed-setup-postgres-equivalent))
- [ ] Apply schema (`doc/sql/azure_sql_bootstrap.sql`) before prod deploy
- [ ] Store connection settings in Key Vault (`azure-sql-*` secrets)
- [ ] Confirm Container App can connect with `SPRING_PROFILES_ACTIVE=prod`
- [ ] Optional: staging database for pre-prod pipeline deploys

### Key Vault & pipeline access

- [ ] Grant pipeline service principal **Key Vault Secrets User** on `kv-hags-prod` (connection `HAGS-ACR-hagsreg`)
- [ ] Create all required secrets (see [azure_variables_and_config_checklist.md](./azure_variables_and_config_checklist.md))
- [ ] Re-run Deploy stage after RBAC propagation (~5–15 min)

---

## Phase 2 — HAG Customer API (this repo)

### Deploy & runtime

- [ ] Green Azure DevOps pipeline (Build + Deploy on `master`)
- [ ] Container App `hags-customer-api` in `rg-hags-prod`, port **8001**, image from ACR
- [ ] Managed identity `hags-ca-mi` has **AcrPull** on `hagsreg`
- [ ] Health check / OpenAPI reachable on ACA FQDN
- [ ] Optional: custom API domain (e.g. `api.hags-grading.co.uk`)

### Shopify ↔ API (both sides)

**Shopify Admin**

- [ ] Webhook URL: `https://<container-app-fqdn>/api/webhooks/shopify`
- [ ] Register topics: `customers/create`, `customers/update`, `orders/paid`, `orders/cancelled` (+ optional `orders/create` / `orders/updated`)
- [ ] Copy webhook signing secret into Key Vault `shopify-webhook-secret`
- [ ] Send test webhook; confirm **200** and rows in `shopify_webhook_events`

**HAG API / Key Vault**

- [ ] `shopify-shop-domain` = `h-a-g-s.myshopify.com`
- [ ] `shopify-webhook-enabled` = `true`, `shopify-webhook-verify-hmac` = `true`
- [ ] End-to-end: `orders/paid` → customer + `purchase_entitlements`
- [ ] Reconciliation job (S5) — periodic backfill for missed webhooks (future)

### Certificates & QR (API side)

- [ ] `qr-cert-secret` in Key Vault
- [ ] `qr.certificate.base-url` points at Static Web App verify URL (not localhost)
- [ ] Test `POST /api/qr-certificate/generate` with real `inspectionId`
- [ ] Test `GET /api/certificates/{serialNumber}` for public lookup

---

## Phase 3 — Ximilar grading API (`HAGS_ximilar_ai`)

- [ ] Repo runs locally: `POST /v1/cards/inspect` → `inspectionId` + grade
- [ ] Ximilar API token in Key Vault
- [ ] Azure deploy (ACA + pipeline, mirror cust API pattern)
- [ ] Blob storage for card images (if used)
- [ ] Cust API / orchestrator can reach grading API
- [ ] E2E: inspect → `POST /api/qr-certificate/generate` → DB `inspection_id` set
- [ ] Orchestrator (later): auto-trigger grading after capture complete

---

## Phase 4 — Certificate verify UI (Static Web App)

- [ ] Provision Azure Static Web App
- [ ] CI/CD from `hags_certificate_lookup_ui` repo
- [ ] Configure API base URL → production cust API
- [ ] Route `/cert/{certificateId}` matches QR URLs
- [ ] CORS on cust API for SWA origin (if browser calls API directly)
- [ ] Custom domain + HTTPS
- [ ] E2E: scan QR → UI shows grade/card

---

## Phase 5 — Cross-cutting & production hardening

- [ ] Application Insights on Container Apps; alerts on 5xx / webhook failures
- [ ] Azure SQL backups (PITR / retention)
- [ ] Shopify order → internal `Submission` workflow defined
- [ ] Customer linking via `shopify_customer_id`; guest checkout edge cases
- [ ] Re-enable or replace disabled Postgres-coupled integration tests (Testcontainers / H2-only)
- [ ] **Future:** Service Bus, capture lane, enrichment, human QA UI, slab printing

---

## Definition of done (your four goals)

| Goal | Done when |
|------|-----------|
| **Azure SQL like Postgres** | Schema on Azure SQL; local Postgres still works; prod app connects via KV secrets |
| **Cust API + KV + SQL + Shopify** | Pipeline green; ACA healthy; secrets set; webhooks 200; paid order writes DB |
| **Ximilar grading pipeline** | `HAGS_ximilar_ai` deployed; inspect → certificate generate works |
| **Static Web App certificate UI** | SWA live; `/cert/{id}` works; QR base URL points here |
| **Webhooks both sides** | Shopify topics + signing secret in KV; HMAC verify passes |

---

## Suggested next 5 actions

1. Key Vault **Secrets User** for pipeline SP + populate `azure-sql-*` and Shopify secrets.
2. Run **`azure_sql_bootstrap.sql`** on Azure SQL; redeploy cust API; confirm startup.
3. Register **Shopify webhooks** to ACA FQDN; fix 401s with matching secret.
4. Deploy **Ximilar service** + one inspect → certificate path.
5. Stand up **Static Web App** + set `qr.certificate.base-url`.

---

# Azure SQL — detailed setup (Postgres equivalent)

Local development uses **PostgreSQL 15** in Docker (`docker-compose.yml`). Production uses **Azure SQL** with the same logical database name and app tables. This section maps every Postgres setting to Azure SQL and walks through provisioning, schema, Key Vault, and verification.

## Postgres vs Azure SQL — configuration map

| Purpose | Postgres (local / Docker) | Azure SQL (prod) |
|--------|---------------------------|------------------|
| **Profile** | `SPRING_PROFILES_ACTIVE=dev` | `SPRING_PROFILES_ACTIVE=prod` |
| **Config file** | `application-dev.properties` | `application-prod.properties` |
| **JDBC URL** | `jdbc:postgresql://postgres:5432/hags_customer` | `jdbc:sqlserver://{server}.database.windows.net:1433;database={db};encrypt=true;...` |
| **Driver** | `org.postgresql.Driver` | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| **Database name** | `hags_customer` | `hags_customer` (recommended — same name) |
| **Username** | `hags_user` | SQL login e.g. `hags_user` (you choose) |
| **Password** | `hags_password` | Strong password (Key Vault only) |
| **Port** | `5432` | `1433` |
| **Schema creation** | `spring.jpa.hibernate.ddl-auto=update` (auto on startup) | `ddl-auto=validate` — **tables must exist before deploy** |
| **Env vars (Docker)** | `SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD` | `AZURE_SQL_SERVER`, `AZURE_SQL_DATABASE`, `AZURE_SQL_USERNAME`, `AZURE_SQL_PASSWORD`, `AZURE_SQL_PORT` |
| **Env vars (pipeline)** | N/A (Postgres service container in CI) | Key Vault → Container App env at deploy |

### Docker Compose (Postgres) — reference

From `docker-compose.yml`:

```yaml
POSTGRES_DB: hags_customer
POSTGRES_USER: hags_user
POSTGRES_PASSWORD: hags_password
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/hags_customer
```

### Production (Azure SQL) — equivalent env

Set on Container App (pipeline injects from Key Vault):

```bash
SPRING_PROFILES_ACTIVE=prod
AZURE_SQL_SERVER=hags-sql-xxxxx.database.windows.net   # no jdbc: prefix
AZURE_SQL_DATABASE=hags_customer
AZURE_SQL_USERNAME=hags_user
AZURE_SQL_PASSWORD=<strong-secret>
AZURE_SQL_PORT=1433
```

Key Vault secret names (dashed) used by `azure-pipelines.yml`:

| Key Vault secret | Maps to env var |
|------------------|-----------------|
| `azure-sql-server` | `AZURE_SQL_SERVER` |
| `azure-sql-database` | `AZURE_SQL_DATABASE` |
| `azure-sql-username` | `AZURE_SQL_USERNAME` |
| `azure-sql-password` | `AZURE_SQL_PASSWORD` |
| `azure-sql-port` | `AZURE_SQL_PORT` |

---

## Step 1 — Create Azure SQL server and database

### Option A — Azure Portal

1. **Azure Portal** → **Create a resource** → **SQL Database**.
2. **Basics**
   - Subscription: `28f3f51f-dea2-4c31-8944-7ff34b40dc96`
   - Resource group: `rg-hags-prod` (or `hags` if that is where your vault lives)
   - Database name: **`hags_customer`** (matches Postgres `POSTGRES_DB`)
   - Server: **Create new** e.g. `hags-sql-prod`
   - Region: **UK South** (align with `kv-hags-prod` / Container Apps)
3. **Authentication**
   - Choose **Use SQL authentication**
   - Server admin login: e.g. `hagsadmin` (server-level; keep in KV separately if needed)
   - Password: strong; store in password manager / KV
4. **Networking**
   - For initial setup from your PC: **Add current client IP**
   - Enable **Allow Azure services and resources to access this server** (required for Container Apps)
   - Later: restrict to private endpoint if needed
5. **Review + create**.

### Option B — Azure CLI

```bash
az account set --subscription 28f3f51f-dea2-4c31-8944-7ff34b40dc96

RG=rg-hags-prod
LOCATION=uksouth
SQL_SERVER=hags-sql-prod
SQL_ADMIN=hagsadmin
SQL_ADMIN_PASSWORD='ChangeMe-Strong-Password-123!'   # use a real secret
SQL_DB=hags_customer

az sql server create \
  --name "$SQL_SERVER" \
  --resource-group "$RG" \
  --location "$LOCATION" \
  --admin-user "$SQL_ADMIN" \
  --admin-password "$SQL_ADMIN_PASSWORD"

az sql server firewall-rule create \
  --resource-group "$RG" \
  --server "$SQL_SERVER" \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# Your dev machine (replace with your public IP)
az sql server firewall-rule create \
  --resource-group "$RG" \
  --server "$SQL_SERVER" \
  --name AllowDevClient \
  --start-ip-address 1.2.3.4 \
  --end-ip-address 1.2.3.4

az sql db create \
  --resource-group "$RG" \
  --server "$SQL_SERVER" \
  --name "$SQL_DB" \
  --service-objective S0 \
  --backup-storage-redundancy Local
```

Note the FQDN: **`hags-sql-prod.database.windows.net`** — this is the value for `AZURE_SQL_SERVER` / Key Vault `azure-sql-server`.

---

## Step 2 — Create application SQL login (Postgres `hags_user` equivalent)

Postgres uses `hags_user` / `hags_password`. On Azure SQL, create a dedicated login mapped to `hags_customer` (do not use the server admin in the app).

Connect as server admin (Azure Portal **Query editor**, or `sqlcmd`, or Azure Data Studio):

```sql
-- Run against master database
CREATE LOGIN hags_user WITH PASSWORD = 'YourStrongAppPassword!ChangeMe';
GO

-- Run against hags_customer database
USE hags_customer;
GO

CREATE USER hags_user FOR LOGIN hags_user;
GO

ALTER ROLE db_datareader ADD MEMBER hags_user;
ALTER ROLE db_datawriter ADD MEMBER hags_user;
ALTER ROLE db_ddladmin ADD MEMBER hags_user;   -- only needed if you use ddl-auto=update once; remove for strict prod
GO
```

For production after schema is applied, you can use only `db_datareader` + `db_datawriter` and omit `db_ddladmin`.

---

## Step 3 — Apply schema (equivalent to Postgres `ddl-auto=update`)

**Postgres (dev):** Hibernate creates/updates tables on every app start (`ddl-auto=update`).

**Azure SQL (prod):** `ddl-auto=validate` — the app **will not start** if tables are missing or columns differ.

### Recommended: run bootstrap SQL script

A T-SQL script mirrors the current JPA entities + Shopify tables (same as Hibernate would create on Postgres):

**File:** [`doc/sql/azure_sql_bootstrap.sql`](./sql/azure_sql_bootstrap.sql)

**Azure Portal Query editor**

1. SQL server → **Query editor** → sign in as admin.
2. Select database **`hags_customer`**.
3. Paste and run the full script.

**sqlcmd (from your machine)**

```bash
sqlcmd -S hags-sql-prod.database.windows.net -d hags_customer -U hags_user -P 'YourStrongAppPassword!ChangeMe' -i doc/sql/azure_sql_bootstrap.sql
```

**Azure CLI + query**

```bash
az sql db query \
  --server hags-sql-prod \
  --database hags_customer \
  --admin-user hagsadmin \
  --admin-password "$SQL_ADMIN_PASSWORD" \
  --query-file doc/sql/azure_sql_bootstrap.sql
```

### Alternative: one-time Hibernate `update` (bootstrap only)

Only on an **empty** Azure SQL database:

1. Temporarily set in `application-prod.properties` (or env override):  
   `spring.jpa.hibernate.ddl-auto=update`
2. Run the app locally pointing at Azure SQL with prod env vars.
3. Confirm tables exist.
4. Revert to `validate` and redeploy.

Do not leave `update` enabled in production long term.

### Postgres migration files note

Files under `src/main/resources/db/migration/` (`V1`–`V4`) use **PostgreSQL syntax** (`BIGSERIAL`, `DO $$`, etc.). They are **not** run automatically in prod today (Flyway is not configured). Use `azure_sql_bootstrap.sql` for Azure SQL, not those files directly.

---

## Step 4 — Store secrets in Key Vault

```bash
KV=kv-hags-prod

az keyvault secret set --vault-name $KV --name azure-sql-server --value "hags-sql-prod.database.windows.net"
az keyvault secret set --vault-name $KV --name azure-sql-database --value "hags_customer"
az keyvault secret set --vault-name $KV --name azure-sql-username --value "hags_user"
az keyvault secret set --vault-name $KV --name azure-sql-password --value "YourStrongAppPassword!ChangeMe"
az keyvault secret set --vault-name $KV --name azure-sql-port --value "1433"
```

Also set Shopify and QR secrets per [azure_variables_and_config_checklist.md](./azure_variables_and_config_checklist.md).

---

## Step 5 — Grant pipeline access to Key Vault

The Deploy stage `AzureKeyVault@2` task uses service connection **`HAGS-ACR-hagsreg`**. Its service principal needs:

- Role: **Key Vault Secrets User**
- Scope: `kv-hags-prod`

```bash
SP_OBJECT_ID=d25c1bde-a871-4c3c-8d20-035b5f2b045e   # from pipeline error, or look up in DevOps

VAULT_ID=$(az keyvault show --name kv-hags-prod --query id -o tsv)

az role assignment create \
  --assignee-object-id "$SP_OBJECT_ID" \
  --assignee-principal-type ServicePrincipal \
  --role "Key Vault Secrets User" \
  --scope "$VAULT_ID"
```

Wait 5–15 minutes, then re-run the pipeline.

---

## Step 6 — Verify connectivity

### From your PC (same as testing Postgres on localhost:5432)

```bash
export SPRING_PROFILES_ACTIVE=prod
export AZURE_SQL_SERVER=hags-sql-prod.database.windows.net
export AZURE_SQL_DATABASE=hags_customer
export AZURE_SQL_USERNAME=hags_user
export AZURE_SQL_PASSWORD='YourStrongAppPassword!ChangeMe'
export AZURE_SQL_PORT=1433

mvn spring-boot:run
```

Look for:

- `The following profiles are active: prod`
- No `Schema-validation: missing table` errors
- App listening on port **8001**

### Quick SQL check

```sql
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
```

Expected tables include: `customers`, `submissions`, `submission_items`, `submission_intake_codes`, `card_certificate`, `card_image`, `shopify_webhook_events`, `purchase_entitlements`.

### After Container App deploy

```bash
az containerapp logs show \
  --name hags-customer-api \
  --resource-group rg-hags-prod \
  --tail 50
```

Confirm no datasource / validation failures.

---

## Step 7 — CI vs prod database

| Environment | Database | How schema is created |
|-------------|----------|------------------------|
| **Local / Docker** | Postgres `hags_customer` | `ddl-auto=update` on app start |
| **Azure Pipelines Build** | Postgres 15 service container | `ddl-auto=create-drop` in `application-test.properties` (tests only) |
| **Azure prod (ACA)** | Azure SQL `hags_customer` | `azure_sql_bootstrap.sql` + `ddl-auto=validate` |

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Schema-validation: missing table [...]` | Bootstrap not run | Run `doc/sql/azure_sql_bootstrap.sql` |
| `REFERENCES permission was denied ... database 'master'` | Script ran on **master** | In Query Editor, select app DB (`free-sql-db-5015439`), not master; re-run bootstrap |
| `Login failed for user` | Wrong user/password in KV | Update `azure-sql-username` / `azure-sql-password` |
| `Cannot open server` / timeout | Firewall | Allow Azure services; add client IP for local tests |
| Key Vault errors in pipeline | SP lacks RBAC | **Key Vault Secrets User** on `kv-hags-prod` |
| `encrypt` / SSL errors | JDBC URL | Use default `application-prod.properties` URL (encrypt=true) |
| Works locally on Postgres, fails on Azure | Type mismatch | Re-run bootstrap; do not copy Postgres-only migration files verbatim |

---

## Tables created (parity with Postgres dev)

| Table | Purpose |
|-------|---------|
| `customers` | Customer records + Shopify IDs |
| `submissions` | Grading submissions |
| `submission_items` | Cards/lines per submission |
| `submission_intake_codes` | Intake barcode / QR |
| `card_certificate` | Slab certificate registry |
| `card_image` | Certificate images |
| `shopify_webhook_events` | Webhook idempotency log |
| `purchase_entitlements` | Paid order entitlements |

---

*Last updated: aligns with `docker-compose.yml`, `application-dev.properties`, `application-prod.properties`, and JPA entities in this repo.*
