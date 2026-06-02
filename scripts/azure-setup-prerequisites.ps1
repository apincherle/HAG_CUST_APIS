# One-time Azure setup for HAG Customer API (Windows PowerShell).
# 1. Install Azure CLI: https://learn.microsoft.com/cli/azure/install-azure-cli-windows
# 2. Open PowerShell and run:
#      az login
#      cd C:\Tools\Workspace\Auth\hags\HAG_CUST_APIS
#      .\scripts\azure-setup-prerequisites.ps1
# Optional: $env:IMAGE_TAG = "123"   # ACR tag from a successful pipeline build

$ErrorActionPreference = "Stop"

$SubscriptionId = "28f3f51f-dea2-4c31-8944-7ff34b40dc96"
$Location       = "uksouth"
$Rg             = "rg-hags-prod"
$AcaEnv         = "hags-env"
$AcaApp         = "hags-customer-api"
$MiName         = "hags-ca-mi"
$AcrName        = "hagsreg"
$AcrLogin       = "hagsreg.azurecr.io"
$ImageRepo      = "hags-customer-api"
$ImageTag       = if ($env:IMAGE_TAG) { $env:IMAGE_TAG } else { "latest" }
$TargetPort     = "8001"

Write-Host "==> Login (browser opens if needed)"
az login | Out-Null
az account set --subscription $SubscriptionId
az account show --query "{name:name, id:id}" -o table

Write-Host "==> Register providers"
az provider register --namespace Microsoft.App --wait
az provider register --namespace Microsoft.OperationalInsights --wait

Write-Host "==> Resource group"
$rgExists = az group exists --name $Rg
if ($rgExists -eq "true") { Write-Host "$Rg exists" }
else { az group create --name $Rg --location $Location | Out-Null }

Write-Host "==> Container Apps environment"
$envJson = az containerapp env show --name $AcaEnv --resource-group $Rg 2>$null
if ($LASTEXITCODE -eq 0) { Write-Host "$AcaEnv exists" }
else {
  az containerapp env create --name $AcaEnv --resource-group $Rg --location $Location
}

Write-Host "==> Managed identity"
$miJson = az identity show --name $MiName --resource-group $Rg 2>$null
if ($LASTEXITCODE -eq 0) { Write-Host "$MiName exists" }
else {
  az identity create --name $MiName --resource-group $Rg --location $Location | Out-Null
}

$miPrincipal = az identity show --name $MiName --resource-group $Rg --query principalId -o tsv
$acrId       = az acr show --name $AcrName --query id -o tsv
Write-Host "==> AcrPull on $AcrName"
az role assignment create --assignee $miPrincipal --role "AcrPull" --scope $acrId 2>$null

$identityId = az identity show --name $MiName --resource-group $Rg --query id -o tsv
$imageRef   = "${AcrLogin}/${ImageRepo}:${ImageTag}"

Write-Host "==> Container App"
$appJson = az containerapp show --name $AcaApp --resource-group $Rg 2>$null
if ($LASTEXITCODE -eq 0) {
  Write-Host "$AcaApp exists — pipeline will update it"
  az containerapp show --name $AcaApp --resource-group $Rg --query "properties.configuration.ingress.fqdn" -o tsv
} else {
  Write-Host "Creating $AcaApp with image $imageRef"
  az extension add --name containerapp --upgrade --only-show-errors
  az containerapp create `
    --name $AcaApp `
    --resource-group $Rg `
    --environment $AcaEnv `
    --image $imageRef `
    --target-port $TargetPort `
    --ingress external `
    --registry-server $AcrLogin `
    --user-assigned $identityId `
    --registry-identity $identityId `
    --query "properties.configuration.ingress.fqdn" -o tsv
}

Write-Host "Done. Re-run Azure DevOps Deploy on master."
