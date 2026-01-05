#!/bin/bash

# Create folder structure for all services

echo "📁 Creating package structure for all services..."

# Array of services
services=("order" "inventory" "payment" "shipping" "notification")

for service in "${services[@]}"
do
    echo "Creating structure for ${service}-service..."
    
    BASE_DIR="${service}-service/src/main/java/com/payment/${service}"
    
    # Create directories
    mkdir -p "${BASE_DIR}/controller"
    mkdir -p "${BASE_DIR}/service"
    mkdir -p "${BASE_DIR}/repository"
    mkdir -p "${BASE_DIR}/model"
    mkdir -p "${BASE_DIR}/event"
    mkdir -p "${BASE_DIR}/config"
    mkdir -p "${BASE_DIR}/dto"
    
    echo "✓ ${service}-service structure created"
done

echo ""
echo "✅ All package structures created!"
echo ""
echo "Structure for each service:"
echo "  controller/  - REST endpoints"
echo "  service/     - Business logic"
echo "  repository/  - Database access"
echo "  model/       - JPA entities"
echo "  event/       - Kafka event models"
echo "  config/      - Configuration classes"
echo "  dto/         - Data transfer objects"