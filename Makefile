# Makefile for building and running the Spring Boot + MyBatis project

# Variables
PROJECT_NAME := frauddetector
JAR_NAME := frauddetector-0.0.1-SNAPSHOT.jar
DOCKER_IMAGE_NAME := frauddetector
DOCKER_CONTAINER_NAME := frauddetector-container
DOCKER_DB_NAME := my-mariadb
DOCKER_PORT := 8080
DOCKER_NET_NAME := local-app-network
CURRENT_DIR := $(CURDIR)

# Default target
all: build

# Build the project
build: docker-db-run
	mvn clean package
	mvn clean install

# Build the Docker image
docker-build:
	docker build -t $(DOCKER_IMAGE_NAME):latest .

# Run the Docker container
docker-run: docker-build docker-db-run
	docker run -d -p $(DOCKER_PORT):$(DOCKER_PORT) \
		--name $(DOCKER_CONTAINER_NAME) \
		--network $(DOCKER_NET_NAME) \
		-e SPRING_DATASOURCE_URL=jdbc:mariadb://$(DOCKER_DB_NAME):3306/demo_db?useSSL=false\&serverTimezone=UTC \
		-e SPRING_DATASOURCE_USERNAME=remote_user \
		-e SPRING_DATASOURCE_PASSWORD=123456 \
		$(DOCKER_IMAGE_NAME):latest

# Stop and remove the Docker container
docker-stop:
	docker stop $(DOCKER_CONTAINER_NAME)
	docker rm $(DOCKER_CONTAINER_NAME)

# Remove the Docker image
docker-rmi:
	docker rmi $(DOCKER_IMAGE_NAME):latest

# Clean the project
clean:
	mvn clean

# Run all tests
test: docker-db-run
	mvn test

# Run a MariaDB container with initialized data
docker-db-run: create-network
	sudo mkdir -p /opt/mariadb/data
	sudo chmod 777 /opt/mariadb/data
	docker run -d --name $(DOCKER_DB_NAME) \
		--network $(DOCKER_NET_NAME) \
		-p 3306:3306 \
		-e MYSQL_ROOT_PASSWORD=123456 \
		-v $(CURRENT_DIR)/init.sql:/docker-entrypoint-initdb.d/init.sql \
		-v /opt/mariadb/data:/var/lib/mysql \
		mariadb:latest

# Stop and remove the MariaDB container
docker-db-stop:
	docker stop $(DOCKER_DB_NAME)
	docker rm $(DOCKER_DB_NAME)

# Create Docker network if it doesn't exist
create-network:
	-@docker network inspect $(DOCKER_NET_NAME) > /dev/null 2>&1; \
	if [ $$? -eq 1 ]; then \
		docker network create $(DOCKER_NET_NAME); \
	fi

# Local Demo docker-compose
run:
	$(MAKE) build
	$(MAKE) docker-build
	$(MAKE) docker-db-stop
	docker-compose -f ./docker-compose.yml up -d

stop:
	docker-compose -f ./docker-compose.yml down

# Kubernetes deployment
k8s-prepare:
	kubectl create namespace fraud-detector-ns
	kubectl get namespaces

k8s-deploy:
	helm install my-fraud-detector helm/fraud-detector-chart/fraud-detector \
		--namespace fraud-detector-ns \
		--set app.image.repository=frauddetector \
		--set app.image.tag=latest \
		--set app.port=8080 \
		--set app.name=fraud-detector \
		--set db.image.repository=mariadb \
		--set db.image.tag=latest \
		--set db.rootPassword="123456" \
		--set db.database="demo_db" \
		--set db.username="remote_user" \
		--set db.password="123456" \
		--set db.name=fraud-detector-db \
		--set service.port=8080 \
		--set service.type=ClusterIP \
		--set service.name=fraud-detector-service \
		--set replicaCount=3 \
		--set autoscaling.enabled=true \
		--set autoscaling.minReplicas=3 \
		--set autoscaling.maxReplicas=10 \
		--set autoscaling.targetCPUUtilizationPercentage=80 \
		--set serviceAccount.create=true \
		--set serviceAccount.name=fraud-detector-sa \
		--set ingress.enabled=false
	kubectl port-forward svc/fraud-detector-service 8080:8080 -n fraud-detector-ns

k8s-delete:
	helm delete my-fraud-detector --namespace fraud-detector-ns

k8s-list:
	kubectl get pods -n fraud-detector-ns
	kubectl get services -n fraud-detector-ns
	kubectl get hpa -n fraud-detector-ns

# Help message
help:
	@echo "Usage:"
	@echo "  make build          - Build the project"
	@echo "  make docker-build   - Build the Docker image"
	@echo "  make docker-run     - Run the Docker container"
	@echo "  make docker-stop    - Stop and remove the Docker container"
	@echo "  make docker-rmi     - Remove the Docker image"
	@echo "  make clean          - Clean the project"
	@echo "  make test           - Run all tests"
	@echo "  make docker-db-run  - Run a MariaDB container with initialized data"
	@echo "  make docker-db-stop - Stop and remove the MariaDB container"
	@echo "  make run            - Run the project using docker-compose"
	@echo "  make stop           - Stop the project using docker-compose"
	@echo "  make help           - Show this help message"

.PHONY: all build docker-build docker-run docker-stop docker-rmi clean test docker-db-run docker-db-stop run stop help
