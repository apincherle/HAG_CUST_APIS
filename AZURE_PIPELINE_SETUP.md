# Azure Pipeline Setup Instructions

## Prerequisites

1. **Azure Container Registry (ACR)**
   - Your ACR is being deployed in resource group `hags-rs`
   - Note the ACR name once deployment completes (e.g., `hagsregistry`)

2. **Azure Container App**
   - Create a Container App in your resource group named `hags-customer-api`
   - Configure it to use your ACR

3. **Azure DevOps Service Connections**
   - Create a service connection for Azure subscription (named "Azure subscription 1" or update the pipeline)
   - Create a service connection for Azure Container Registry (named "hags-acr" or update the pipeline)

4. **Azure DevOps Pipeline Variables**
   - Go to your pipeline → Edit → Variables
   - Add the following variables (mark as secret for passwords):
     - `SQL_USERNAME` - Azure SQL database username
     - `SQL_PASSWORD` - Azure SQL database password (mark as secret)
     - `QR_CERT_SECRET` - Secret key for QR certificate signing (mark as secret)
   
   **OR** create a variable group:
   - Create a variable group named `hags-variables` in Azure DevOps
   - Add the same variables above
   - Uncomment line 15 in `azure-pipelines.yml`: `# - group: hags-variables`

## Pipeline Configuration

### Variables to Update

1. **containerRegistry** (line 22): Update with your ACR service connection name
2. **azureSubscription** (lines 119, 129, 162): Update with your Azure service connection name
3. **containerAppName** (line 19): Update if your Container App has a different name

### SQL Database Configuration

The pipeline is configured to use:
- **Server**: `hags-s1.database.windows.net`
- **Database**: `free-sql-db-2072015`
- **Resource Group**: `hags-rs`
- **Subscription**: `4d6e1e46-b56b-4cf8-b6bf-de5f41d462d3`

### Environment Variables Set During Deployment

The following environment variables are automatically set in the Container App:
- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL` - SQL Server connection string
- `SPRING_DATASOURCE_USERNAME` - From variable group
- `SPRING_DATASOURCE_PASSWORD` - From variable group
- `AZURE_SQL_SERVER` - SQL server hostname
- `AZURE_SQL_DATABASE` - Database name
- `AZURE_SQL_PORT` - Port (1433)
- `QR_CERTIFICATE_BASE_URL` - QR certificate base URL
- `QR_CERT_SECRET` - From variable group
- `QR_STORAGE_PATH` - Path for QR code storage
- `QR_SERIAL_PREFIX` - Serial number prefix

## Pipeline Stages

1. **Build Stage**: Compiles, tests, and packages the Java application
2. **BuildDocker Stage**: Builds and pushes Docker image to ACR
3. **Deploy Stage**: Deploys the new image to Azure Container App

## Troubleshooting

### Container App Not Found
- Ensure the Container App exists in the resource group
- Verify the name matches `containerAppName` variable

### ACR Connection Issues
- Verify the ACR service connection is configured correctly
- Check that the service principal has AcrPush permissions

### SQL Connection Issues
- Verify SQL credentials in the variable group
- Check that the SQL server firewall allows Azure services
- Ensure the database is not paused (resume if needed)

### Deployment Fails
- Check Azure CLI task logs for detailed error messages
- Verify all environment variables are set correctly
- Ensure the Container App has the correct managed identity permissions

