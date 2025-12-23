# Database Profile Configuration Guide

This project supports two database configurations:
- **Development**: SQLite (file-based, local)
- **Production**: Azure SQL Database

## Quick Start

### Development (SQLite) - Default
```bash
# No profile needed - defaults to dev
mvn spring-boot:run

# Or explicitly set
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Production (Azure SQL)
```bash
export SPRING_PROFILES_ACTIVE=prod
export AZURE_SQL_SERVER=your-server.database.windows.net
export AZURE_SQL_DATABASE=your-database
export AZURE_SQL_USERNAME=your-username
export AZURE_SQL_PASSWORD=your-password
mvn spring-boot:run
```

## Docker

### Development
```bash
docker-compose up -d
```

### Production
```bash
export AZURE_SQL_SERVER=your-server.database.windows.net
export AZURE_SQL_DATABASE=your-database
export AZURE_SQL_USERNAME=your-username
export AZURE_SQL_PASSWORD=your-password
docker-compose -f docker-compose.prod.yml up -d
```

## Configuration Files

- `application.properties` - Base configuration, sets active profile
- `application-dev.properties` - SQLite configuration
- `application-prod.properties` - Azure SQL configuration

## Environment Variables

### Development (SQLite)
- `SPRING_PROFILES_ACTIVE=dev` (optional, default)
- `DB_PATH=./data/dev.db` (optional, default path)

### Production (Azure SQL)
- `SPRING_PROFILES_ACTIVE=prod` (required)
- `AZURE_SQL_SERVER` (required)
- `AZURE_SQL_DATABASE` (required)
- `AZURE_SQL_USERNAME` (required)
- `AZURE_SQL_PASSWORD` (required)
- `AZURE_SQL_PORT` (optional, default: 1433)

## Switching Profiles

### Method 1: Environment Variable
```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

### Method 2: Command Line Argument
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Method 3: System Property
```bash
java -jar target/hags-customer-api-1.0-SNAPSHOT.jar -Dspring.profiles.active=prod
```

### Method 4: Docker Compose
Edit `docker-compose.yml`:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

## Database Locations

- **SQLite (dev)**: `./data/dev.db` (local) or `/app/data/dev.db` (Docker)
- **Azure SQL (prod)**: Remote Azure SQL Database

## Verification

Check which profile is active:
```bash
# Check logs when starting the application
# Look for: "The following profiles are active: dev" or "prod"
```

Or check the database connection in logs:
- Dev: `jdbc:sqlite:./data/dev.db`
- Prod: `jdbc:sqlserver://...`

