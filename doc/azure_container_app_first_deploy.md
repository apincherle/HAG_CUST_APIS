# First deploy — create Container App once (Portal)

The Azure DevOps pipeline **only updates the Docker image** on an existing Container App.  
It does **not** run `az containerapp create` (that command hits an Azure CLI bug in CI).

Do this **once** in the Portal, then every pipeline run only swaps the image.

## Prerequisites

| Item | Name |
|------|------|
| Resource group | `rg-hags-prod` |
| Container Apps environment | `hags-env` |
| Managed identity | `hags-ca-mi` (AcrPull on `hagsreg`) |
| ACR image | `hagsreg.azurecr.io/hags-customer-api:<BuildId>` from a green **Build** stage |

## Portal steps

1. **Container Apps** → **+ Create**
2. **Basics:** RG `rg-hags-prod`, name `hags-customer-api`, region UK South, environment `hags-env`
3. **Container:** Image from ACR (pick the tag from your last successful build)
4. **Ingress:** Enabled, external, target port **8001**
5. **Registry:** `hagsreg.azurecr.io`, auth = user-assigned **`hags-ca-mi`**
6. **Environment variables** (set manually — same values as Key Vault):

   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `AZURE_SQL_SERVER` = `hags.database.windows.net`
   - `AZURE_SQL_DATABASE` = `hags_customer`
   - `AZURE_SQL_USERNAME` = `adminhags@hags`
   - `AZURE_SQL_PASSWORD` = *(secret)*
   - `AZURE_SQL_PORT` = `1433`
   - `SHOPIFY_WEBHOOK_SECRET`, `SHOPIFY_SHOP_DOMAIN`, `SHOPIFY_WEBHOOK_ENABLED`, `SHOPIFY_WEBHOOK_VERIFY_HMAC`, `QR_CERT_SECRET`

7. **Create**

## Pipeline service principal permissions

The app can exist in Portal but deploy still fails with "not found" if the **Azure DevOps service connection** (`HAGS-ACR-hagsreg`) cannot read/update Container Apps.

On **`rg-hags-prod`** → **Access control (IAM)** → add for that service principal:

- **Contributor**, or  
- **Container Apps Contributor**

(Key Vault access alone is not enough.)

## After that

Push to `master` → pipeline **Deploy** runs:

```text
az containerapp update --image hagsreg.azurecr.io/hags-customer-api:<BuildId>
```

Env vars stay as you set them in Portal unless you change them there.

## Optional: re-enable Key Vault in pipeline later

If you want the pipeline to refresh env vars from Key Vault on each deploy, that can be added back to `azure-pipelines.yml` after the app exists (update-only, with `--set-env-vars`).

## Health probes (app starts then stops / “not working”)

Logs like `Started HagsCustomerApplication` followed by `Closing JPA EntityManagerFactory` mean the JVM received **SIGTERM** (new revision rollout, scale-down, or failed probe). The API had **no** root `/` endpoint; probes should use Actuator after the next deploy.

**Portal:** Container App → **Containers** → your container → **Health probes** (or **Scale** / revision health):

| Probe | Type | Port | Path |
| ----- | ---- | ---- | ---- |
| Startup (optional) | HTTP | `8001` | `/actuator/health` |
| Liveness | HTTP | `8001` | `/actuator/health` |
| Readiness | HTTP | `8001` | `/actuator/health/readiness` |

Remove probes that point at `/` unless you add a handler there.

**Verify after deploy** (from a machine that can reach the app):

```text
https://<fqdn>/actuator/health
```

Expect `{"status":"UP"}`.

## Internal vs external ingress

FQDN containing **`.internal.`** (e.g. `hags-customer-api.internal.bravecliff-....azurecontainerapps.io`) is **not reachable from the public internet**. Swagger and webhooks from Shopify require **external** ingress (or VPN / VNet integration to the Container Apps environment).

**Portal:** Container App → **Ingress** → set **Ingress** to **Enabled** and **Ingress traffic** to **Accepting traffic from anywhere** (external) if you need browser/Shopify access.

**URLs** (replace FQDN):

| What | URL |
| ---- | --- |
| Health | `https://<fqdn>/actuator/health` |
| Swagger UI | `https://<fqdn>/swagger-ui.html` |
| OpenAPI JSON | `https://<fqdn>/v3/api-docs` |
