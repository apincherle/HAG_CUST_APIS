#!/usr/bin/env bash
# One-time Azure setup for HAG Customer API pipeline deploy.
# Run in Azure Cloud Shell (bash): https://shell.azure.com
#   curl -sL <raw-url> | bash
# Or locally: az login && bash scripts/azure-setup-prerequisites.sh
#
# Optional: set IMAGE_TAG to an existing ACR tag before create (default: latest)
#   IMAGE_TAG=123 bash scripts/azure-setup-prerequisites.sh

set -euo pipefail

SUBSCRIPTION_ID="28f3f51f-dea2-4c31-8944-7ff34b40dc96"
LOCATION="uksouth"
RG="rg-hags-prod"
ACA_ENV="hags-env"
ACA_APP="hags-customer-api"
MI_NAME="hags-ca-mi"
ACR_NAME="hagsreg"
ACR_LOGIN="hagsreg.azurecr.io"
IMAGE_REPO="hags-customer-api"
IMAGE_TAG="${IMAGE_TAG:-latest}"
TARGET_PORT="8001"
KV_NAME="kv-hags-prod"

echo "==> Subscription"
az account set --subscription "$SUBSCRIPTION_ID"
az account show --query "{name:name, id:id}" -o table

echo "==> Register resource providers (fixes containerapp create IndexError)"
az provider register --namespace Microsoft.App --wait
az provider register --namespace Microsoft.OperationalInsights --wait
az provider show --namespace Microsoft.App --query registrationState -o tsv
az provider show --namespace Microsoft.OperationalInsights --query registrationState -o tsv

echo "==> Resource group"
if az group show --name "$RG" &>/dev/null; then
  echo "Resource group $RG already exists"
else
  az group create --name "$RG" --location "$LOCATION"
fi

echo "==> Container Apps environment"
if az containerapp env show --name "$ACA_ENV" --resource-group "$RG" &>/dev/null; then
  echo "Environment $ACA_ENV already exists"
else
  az containerapp env create \
    --name "$ACA_ENV" \
    --resource-group "$RG" \
    --location "$LOCATION"
fi

echo "==> User-assigned managed identity"
if az identity show --name "$MI_NAME" --resource-group "$RG" &>/dev/null; then
  echo "Identity $MI_NAME already exists"
else
  az identity create --name "$MI_NAME" --resource-group "$RG" --location "$LOCATION"
fi

MI_PRINCIPAL_ID="$(az identity show --name "$MI_NAME" --resource-group "$RG" --query principalId -o tsv)"
ACR_ID="$(az acr show --name "$ACR_NAME" --query id -o tsv)"

echo "==> AcrPull for $MI_NAME on $ACR_NAME"
az role assignment create \
  --assignee "$MI_PRINCIPAL_ID" \
  --role "AcrPull" \
  --scope "$ACR_ID" \
  2>/dev/null || echo "(AcrPull may already exist — OK)"

IDENTITY_RESOURCE_ID="$(az identity show --name "$MI_NAME" --resource-group "$RG" --query id -o tsv)"
IMAGE_REF="${ACR_LOGIN}/${IMAGE_REPO}:${IMAGE_TAG}"

echo "==> Container App (create only if missing)"
if az containerapp show --name "$ACA_APP" --resource-group "$RG" &>/dev/null; then
  echo "Container App $ACA_APP already exists — pipeline will use 'update' only"
  az containerapp show --name "$ACA_APP" --resource-group "$RG" \
    --query "properties.configuration.ingress.fqdn" -o tsv
else
  echo "Creating $ACA_APP with image $IMAGE_REF"
  echo "(Set IMAGE_TAG to your pipeline BuildId if 'latest' does not exist in ACR)"
  az containerapp create \
    --name "$ACA_APP" \
    --resource-group "$RG" \
    --environment "$ACA_ENV" \
    --image "$IMAGE_REF" \
    --target-port "$TARGET_PORT" \
    --ingress external \
    --registry-server "$ACR_LOGIN" \
    --user-assigned "$IDENTITY_RESOURCE_ID" \
    --registry-identity "$IDENTITY_RESOURCE_ID" \
    --query "properties.configuration.ingress.fqdn" -o tsv
  echo "Add env vars in Portal (Container App -> Containers -> Environment variables)"
  echo "or re-run Azure DevOps Deploy stage to set them from Key Vault."
fi

echo ""
echo "Done. Key Vault secrets stay in: $KV_NAME"
echo "Re-run pipeline Deploy stage on master."
