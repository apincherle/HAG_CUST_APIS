# Azure SQL connection reference (HAG Customer API)

Non-secret values for the production database. Passwords belong only in **Key Vault** or `config/azure-sql.env` (gitignored).

## Resource summary

| Setting | Value |
|---------|--------|
| SQL server | `hags` |
| Server FQDN | `hags.database.windows.net` |
| Database | `hags_customer` |
| Port | `1433` |
| SQL login | `adminhags@hags` (use `@hags` suffix for Spring / JDBC; some tools show `adminhags` only) |
| Spring profile | `prod` |
| Schema bootstrap | [`sql/azure_sql_bootstrap.sql`](./sql/azure_sql_bootstrap.sql) (run once on `hags_customer`, not `master`) |

## Spring Boot / Container App env vars

```bash
SPRING_PROFILES_ACTIVE=prod
AZURE_SQL_SERVER=hags.database.windows.net
AZURE_SQL_DATABASE=hags_customer
AZURE_SQL_USERNAME=adminhags@hags
AZURE_SQL_PASSWORD=<from Key Vault>
AZURE_SQL_PORT=1433
```

Built JDBC URL (from `application-prod.properties`):

```text
jdbc:sqlserver://hags.database.windows.net:1433;database=hags_customer;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

## Key Vault (`kv-hags-prod`)

Set these secret **names** (values are non-secret except password):

| Secret name | Example value |
|-------------|----------------|
| `azure-sql-server` | `hags.database.windows.net` |
| `azure-sql-database` | `hags_customer` |
| `azure-sql-username` | `adminhags@hags` |
| `azure-sql-password` | *(your SQL password)* |
| `azure-sql-port` | `1433` |

```bash
az account set --subscription 28f3f51f-dea2-4c31-8944-7ff34b40dc96
KV=kv-hags-prod

az keyvault secret set --vault-name $KV --name azure-sql-server   --value "hags.database.windows.net"
az keyvault secret set --vault-name $KV --name azure-sql-database --value "hags_customer"
az keyvault secret set --vault-name $KV --name azure-sql-username --value "adminhags@hags"
az keyvault secret set --vault-name $KV --name azure-sql-password --value "<your-password>"
az keyvault secret set --vault-name $KV --name azure-sql-port     --value "1433"
```

## DBeaver / Azure Data Studio

| Field | Value |
|-------|--------|
| Host | `hags.database.windows.net` |
| Database | `hags_customer` |
| Username | `adminhags@hags` (if login fails, try `adminhags`) |
| Authentication | SQL Server Authentication |
| Encrypt | Yes |
| Trust server certificate | No (default for Azure) |

## Local run with prod profile

```bash
# Windows PowerShell — load from gitignored env file after copying the example
Get-Content config/azure-sql.env | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]+)=(.*)$') { Set-Item -Path "env:$($matches[1].Trim())" -Value $matches[2].Trim() }
}
mvn spring-boot:run
```

Or copy [`config/azure-sql.env.example`](../config/azure-sql.env.example) → `config/azure-sql.env` and fill in the password.

## Related docs

- [Azure variables checklist](./azure_variables_and_config_checklist.md)
- [Platform delivery todo](./HAG_platform_delivery_todo.md)
- [azure/hags-azure-details.yml](../azure/hags-azure-details.yml)
