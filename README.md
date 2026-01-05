# Payment Processing Simulator

Event-driven order fulfillment system with 5 microservices communicating via Apache Kafka.

## Architecture

- **Order Service** (Port 8081): Orchestrates the order saga
- **Inventory Service** (Port 8082): Manages product stock
- **Payment Service** (Port 8083): Processes payments with fraud detection
- **Shipping Service** (Port 8084): Handles shipment creation
- **Notification Service** (Port 8085): Logs all system events

## Quick Start

1. Start infrastructure:
   ```bash
   docker-compose up -d
   ```

2. Build and run each service:
   ```bash
   cd order-service && mvn spring-boot:run
   ```

3. Access Kafka UI: http://localhost:8080

## Tech Stack

- Java 17
- Spring Boot 3.2
- Apache Kafka
- PostgreSQL
- Redis
- Docker
