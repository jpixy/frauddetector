# Makefile for building and running the Spring Boot + MyBatis project

# Variables
PROJECT_NAME := frauddetector
JAR_NAME := frauddetector-0.0.1-SNAPSHOT.jar
DOCKER_IMAGE_NAME := frauddetector
DOCKER_CONTAINER_NAME := frauddetector-container
DOCKER_PORT := 8080
CURRENT_DIR := $(CURDIR)

# Default target
all: build

# Build the project
build:
	mvn clean package
	mvn clean install

# Build the Docker image
docker-build:
	docker build -t $(DOCKER_IMAGE_NAME):latest .

# Run the Docker container
docker-run:
	docker run -d -p $(DOCKER_PORT):$(DOCKER_PORT) --name $(DOCKER_CONTAINER_NAME) $(DOCKER_IMAGE_NAME):latest

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
test:
	mvn test

# Run a MariaDB container with initialized data
docker-db:
	sudo mkdir -p /opt/mariadb/data
	sudo chmod 777 /opt/mariadb/data
	docker run -d --name my-mariadb -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 \
	  -v $(CURRENT_DIR)/init.sql:/docker-entrypoint-initdb.d/init.sql \
	  -v /opt/mariadb/data:/var/lib/mysql \
	  mariadb:latest

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
	@echo "  make docker-db      - Run a MariaDB container with initialized data"
	@echo "  make help           - Show this help message"

.PHONY: all build docker-build docker-run docker-stop docker-rmi clean test help