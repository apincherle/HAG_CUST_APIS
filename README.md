# Simple REST API with JPA/SQL

A basic Spring Boot REST API that maps JSON to Java POJOs and stores data in a SQL database using JPA.

## Features

- REST API with GET, POST, PUT, DELETE endpoints
- JSON to Java POJO mapping (automatic via Spring Boot)
- JPA for SQL database operations
- **SQLite for development** (persisted in `./data/dev.db`)
- **Azure SQL for production** (configurable via environment variables)
- Easy profile switching between dev and prod environments

## API Endpoints

### Items API

- `GET /api/items` - Get all items
- `GET /api/items/{id}` - Get item by ID
- `POST /api/items` - Create a new item
- `PUT /api/items/{id}` - Update an existing item
- `DELETE /api/items/{id}` - Delete an item
- `GET /api/items/search?name={name}` - Search items by name

## Example Requests

### Create an Item (POST)
```bash
curl -X POST http://localhost:8001/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Item",
    "description": "This is a sample item",
    "price": 29.99
  }'
```

### SWAGGER
```bash
http://localhost:8001/swagger-ui/index.html
```

### Get All Items (GET)
```bash
curl http://localhost:8001/api/items
```

### Get Item by ID (GET)
```bash
curl http://localhost:8001/api/items/1
```

### Update Item (PUT)
```bash
curl -X PUT http://localhost:8001/api/items/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Item",
    "description": "Updated description",
    "price": 39.99
  }'
```

### Delete Item (DELETE)
```bash
curl -X DELETE http://localhost:8001/api/items/1
```

## Running the Application

### Development Mode (SQLite)

1. Build the project:
```bash
mvn clean package
```

2. Run with dev profile (default):
```bash
mvn spring-boot:run
```

Or explicitly set the profile:
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

The SQLite database will be created at `./data/dev.db` and will persist between runs.

### Production Mode (Azure SQL)

Set environment variables and run with prod profile:
```bash
export SPRING_PROFILES_ACTIVE=prod
export AZURE_SQL_SERVER=your-server.database.windows.net
export AZURE_SQL_DATABASE=your-database
export AZURE_SQL_USERNAME=your-username
export AZURE_SQL_PASSWORD=your-password
mvn spring-boot:run
```

### Docker Compose (Development)

Run with Docker Compose (uses SQLite):
```bash
docker-compose up -d
```

The API will be available at `http://localhost:8001`

## Database Configuration

### Development (SQLite)
- **Database**: SQLite file-based database
- **Location**: `./data/dev.db`
- **Profile**: `dev` (default)
- **Configuration**: `application-dev.properties`

### Production (Azure SQL)
- **Database**: Azure SQL Database
- **Profile**: `prod`
- **Configuration**: `application-prod.properties`
- **Environment Variables Required**:
  - `AZURE_SQL_SERVER` - Your Azure SQL server name
  - `AZURE_SQL_DATABASE` - Database name
  - `AZURE_SQL_USERNAME` - Database username
  - `AZURE_SQL_PASSWORD` - Database password
  - `AZURE_SQL_PORT` - Port (default: 1433)

### Switching Profiles

**Option 1: Environment Variable**
```bash
export SPRING_PROFILES_ACTIVE=dev  # or prod
mvn spring-boot:run
```

**Option 2: Command Line**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option 3: Docker Compose**
Edit `docker-compose.yml` and set:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod  # or dev
```

## Project Structure

```
src/main/java/com/example/
├── HagsCustomerApplication.java    # Main Spring Boot application
├── controller/
│   └── ItemController.java          # REST API endpoints
├── model/
│   └── Item.java                    # JPA entity (POJO)
└── repository/
    └── ItemRepository.java          # JPA repository interface
```

## Environment Variables

### Development (SQLite)
No environment variables needed - uses SQLite file database.

### Production (Azure SQL)
Required environment variables:
- `AZURE_SQL_SERVER` - Azure SQL server hostname
- `AZURE_SQL_DATABASE` - Database name
- `AZURE_SQL_USERNAME` - Database username
- `AZURE_SQL_PASSWORD` - Database password
- `AZURE_SQL_PORT` - Port (optional, default: 1433)

Optional:
- `SPRING_PROFILES_ACTIVE` - Set to `prod` for production mode

## Docker

### Build Docker Image
```bash
docker build -t hags-customer-api .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f
```

### Stop
```bash
docker-compose down
```

The SQLite database file is persisted in the `./data` directory.

