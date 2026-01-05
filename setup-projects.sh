#!/bin/bash

# Payment Processing Simulator - Automated Setup Script
# This script creates all 5 Spring Boot microservices

echo "🚀 Setting up Payment Processing Simulator..."

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -d "order-service" ]; then
    echo -e "${BLUE}Creating service directories...${NC}"
    mkdir -p order-service inventory-service payment-service shipping-service notification-service
fi

# Function to create Spring Boot project
create_service() {
    SERVICE_NAME=$1
    PORT=$2
    PACKAGE_NAME=$3
    EXTRA_DEPS=$4
    
    echo -e "${BLUE}Creating $SERVICE_NAME on port $PORT...${NC}"
    
    cd $SERVICE_NAME
    
    # Generate Spring Boot project
    curl https://start.spring.io/starter.zip \
      -d type=maven-project \
      -d language=java \
      -d bootVersion=3.5.0 \
      -d groupId=com.payment \
      -d artifactId=$SERVICE_NAME \
      -d name=$SERVICE_NAME \
      -d packageName=com.payment.$PACKAGE_NAME \
      -d packaging=jar \
      -d javaVersion=17 \
      -d dependencies=web,data-jpa,postgresql,lombok,actuator,validation$EXTRA_DEPS \
      -o project.zip
    
    # Check if the download was actually a zip file
    if file project.zip | grep -q "Zip archive"; then
        unzip -q project.zip
        rm project.zip
    else
        echo -e "${RED}Error: Failed to download a valid ZIP from Spring Initializr.${NC}"
        echo "Response received:"
        cat project.zip
        exit 1
    fi
    
    # Add Kafka dependency to pom.xml
    # This adds Spring Kafka after the </dependencies> tag
    sed -i.bak '/<\/dependencies>/i\
<!-- Spring Kafka -->\
<dependency>\
<groupId>org.springframework.kafka</groupId>\
<artifactId>spring-kafka</artifactId>\
</dependency>\
<dependency>\
<groupId>org.springframework.kafka</groupId>\
<artifactId>spring-kafka-test</artifactId>\
<scope>test</scope>\
</dependency>
' pom.xml
    
    rm pom.xml.bak 2>/dev/null
    
    echo -e "${GREEN}✓ $SERVICE_NAME created${NC}"
    cd ..
}

# Create all services
echo -e "\n${BLUE}Generating Spring Boot projects...${NC}\n"

create_service "order-service" "8081" "order" ""
create_service "inventory-service" "8082" "inventory" ""
create_service "payment-service" "8083" "payment" ",data-redis"
create_service "shipping-service" "8084" "shipping" ""
create_service "notification-service" "8085" "notification" ""

echo -e "\n${GREEN}✓ All services created successfully!${NC}\n"

# Create .gitignore
echo -e "${BLUE}Creating .gitignore...${NC}"
cat > .gitignore << 'EOF'
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml

# IDE
.idea/
*.iml
.vscode/
.classpath
.project
.settings/

# Logs
*.log

# OS
.DS_Store
Thumbs.db

# Application
application-local.yml
application-dev.yml
EOF

echo -e "${GREEN}✓ .gitignore created${NC}\n"

# Create README
echo -e "${BLUE}Creating README.md...${NC}"
cat > README.md << 'EOF'
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
EOF

echo -e "${GREEN}✓ README.md created${NC}\n"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Setup Complete! 🎉${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "Next steps:"
echo -e "1. Start Docker containers: ${BLUE}docker-compose up -d${NC}"
echo -e "2. Copy application.yml files to each service's src/main/resources/"
echo -e "3. Test a service: ${BLUE}cd order-service && mvn spring-boot:run${NC}"
echo -e "4. Access Kafka UI: ${BLUE}http://localhost:8080${NC}\n"