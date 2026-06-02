# Azure variables and config checklist

This file lists everything you need to configure in Azure for `azure-pipelines.yml` to build and deploy successfully.

## 1) Azure DevOps service connection

Create one **Azure Resource Manager** service connection and use this name in pipeline variables:

- `HAGS-ACR-hagsreg`

Requirements for this connection:

- Subscription: `28f3f51f-dea2-4c31-8944-7ff34b40dc96`
- Has permission to:
  - push images to ACR `hagsreg`
  - read secrets from Key Vault `kv-hags-prod`
  - create/update Container Apps in resource group `rg-hags-prod`
- Authorized for this pipeline (grant access to all pipelines or authorize this one explicitly)

## 2) Pipeline variables (in YAML)

Current expected values in `azure-pipelines.yml`:

- `azureSubscription`: `HAGS-ACR-hagsreg`
- `azureSubscriptionId`: `28f3f51f-dea2-4c31-8944-7ff34b40dc96`
- `containerRegistry`: `hagsreg.azurecr.io`
- `acrName`: `hagsreg`
- `imageRepository`: `hags-customer-api`
- `resourceGroup`: `rg-hags-prod`
- `containerAppName`: `hags-customer-api`
- `containerAppEnv`: `hags-env`
- `managedIdentity`: `hags-ca-mi`
- `containerTargetPort`: `8001`
- `KeyVaultName`: `kv-hags-prod`

## 3) Key Vault secrets required (dashed names)

The pipeline fetches these exact Key Vault secret names:

- `azure-sql-server`
- `azure-sql-database`
- `azure-sql-username`
- `azure-sql-password`
- `azure-sql-port`
- `shopify-webhook-secret`
- `shopify-shop-domain`
- `shopify-webhook-enabled`
- `shopify-webhook-verify-hmac`
- `qr-cert-secret`

If any are missing, deploy fails fast with:

`Missing required variable from Key Vault/pipeline: <NAME>`

### Runtime mapping

The deploy step maps these dashed Key Vault names to runtime env vars used by Spring:

- `azure-sql-server` -> `AZURE_SQL_SERVER`
- `azure-sql-database` -> `AZURE_SQL_DATABASE`
- `azure-sql-username` -> `AZURE_SQL_USERNAME`
- `azure-sql-password` -> `AZURE_SQL_PASSWORD`
- `azure-sql-port` -> `AZURE_SQL_PORT`
- `shopify-webhook-secret` -> `SHOPIFY_WEBHOOK_SECRET`
- `shopify-shop-domain` -> `SHOPIFY_SHOP_DOMAIN`
- `shopify-webhook-enabled` -> `SHOPIFY_WEBHOOK_ENABLED`
- `shopify-webhook-verify-hmac` -> `SHOPIFY_WEBHOOK_VERIFY_HMAC`
- `qr-cert-secret` -> `QR_CERT_SECRET`

### Suggested values

- `AZURE_SQL_PORT`: `1433`
- `SHOPIFY_SHOP_DOMAIN`: `h-a-g-s.myshopify.com`
- `SHOPIFY_WEBHOOK_ENABLED`: `true`
- `SHOPIFY_WEBHOOK_VERIFY_HMAC`: `true`

## 4) Azure Container Apps prerequisites

Ensure these resources exist:

- Resource group: `rg-hags-prod`
- Container Apps environment: `hags-env`
- Container App: `hags-customer-api` (or pipeline will create it)
- User-assigned managed identity: `hags-ca-mi`
- ACR: `hagsreg`

The deploy script does:

- `az containerapp update` if app exists
- `az containerapp create` if app does not exist

Both paths set:

- `SPRING_PROFILES_ACTIVE=prod`
- all `AZURE_SQL_*` vars
- Shopify and QR vars

## 5) Identity and ACR pull access

Container App identity (`hags-ca-mi`) needs pull access to ACR:

- Role: `AcrPull`
- Scope: ACR `hagsreg`

## 6) App/runtime settings to verify

- Container ingress: external
- Target port: `8001`
- Image: `hagsreg.azurecr.io/hags-customer-api:<BuildId>`
- Spring profile in runtime env: `prod`

## 7) Shopify webhook URL after deploy

Use your Container App FQDN:

`https://<container-app-fqdn>/api/webhooks/shopify`

Do not use localhost for Shopify delivery.

## 8) Quick validation commands

```bash
az account set --subscription 28f3f51f-dea2-4c31-8944-7ff34b40dc96
az acr show -n hagsreg --query loginServer -o tsv
az keyvault secret list --vault-name kv-hags-prod --query "[].name" -o tsv
az containerapp show -n hags-customer-api -g rg-hags-prod --query "properties.configuration.ingress.fqdn" -o tsv
```

## 9) Common failure reasons

- Service connection is wrong type or not authorized
- Key Vault missing one or more required secret names
- Managed identity lacks `AcrPull`
- Wrong resource group / container app / environment names in variables
- App deployed but wrong port configured (must be `8001`)
