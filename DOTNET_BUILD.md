# .NET Build Instructions

## Prerequisites
- .NET 8 SDK
- Docker & Docker Compose
- Kubernetes (optional)

## Building the Solution

### Local Development
```bash
# Restore dependencies
dotnet restore

# Build all projects
dotnet build

# Run specific service
cd Identity
dotnet run

# Or with watch mode (hot reload)
dotnet watch run
```

### Database Migrations

For Identity and Order services (PostgreSQL):
```bash
# Install Entity Framework tools
dotnet tool install --global dotnet-ef

# Create migration
cd Identity
dotnet ef migrations add InitialCreate

# Apply migration
dotnet ef database update
```

### Docker Build

Build all services:
```bash
# Build specific service
docker build --target identity -t eshoponsteroids-identity .
docker build --target cart -t eshoponsteroids-cart .
docker build --target order -t eshoponsteroids-order .
docker build --target product -t eshoponsteroids-product .
docker build --target api-gateway -t eshoponsteroids-api-gateway .
```

Or use docker-compose:
```bash
docker-compose up --build
```

## Running Tests

```bash
# Run all tests
dotnet test

# Run tests for specific project
cd Identity.Tests
dotnet test
```

## Project Structure

- **Identity**: User authentication and JWT token management (ASP.NET Core, PostgreSQL, Entity Framework Core)
- **Cart**: Shopping cart management (ASP.NET Core, Redis, StackExchange.Redis)
- **Order**: Order processing and fulfillment (ASP.NET Core, PostgreSQL, gRPC, RabbitMQ)
- **Product**: Product catalog and inventory (ASP.NET Core, MongoDB, gRPC server)
- **ApiGateway**: Reverse proxy with authentication (Yarp)
- **Payment**: Payment processing (Python - unchanged)

## Key Technologies

- **ASP.NET Core 8.0**: Web framework
- **Entity Framework Core**: ORM for PostgreSQL
- **MongoDB.Driver**: MongoDB ODM
- **StackExchange.Redis**: Redis client
- **Yarp**: Reverse proxy
- **Grpc.AspNetCore**: gRPC server
- **Grpc.Net.Client**: gRPC client
- **RabbitMQ.Client**: Message broker
- **Polly**: Resilience and circuit breaker
- **OpenTelemetry**: Distributed tracing
- **Prometheus**: Metrics
- **BCrypt.Net**: Password hashing

## Environment Variables

See `.env` file for required environment variables. Key variables:
- Database connection strings (PostgreSQL, MongoDB, Redis)
- RabbitMQ configuration
- JWT settings
- Service URLs
- Zipkin endpoint

## gRPC Configuration

The Product service exposes a gRPC endpoint on port 9898 for inter-service communication. The Order service consumes this endpoint with circuit breaker protection via Polly.

## Notes

- All services use OpenTelemetry for distributed tracing
- Prometheus metrics are exposed on `/metrics` endpoint
- Health checks available at `/actuator/health`
- API documentation via Swagger at `/swagger`
