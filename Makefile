# Makefile for building and running the Spring Boot + MyBatis project

# Variables
PROJECT_NAME=frauddetector
JAR_NAME=frauddetector-0.0.1-SNAPSHOT.jar
DOCKER_IMAGE_NAME=frauddetector
DOCKER_CONTAINER_NAME=frauddetector-container
DOCKER_PORT=8080

# Default target
all: build

# Build the project
build:
	mvn clean package

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

# Help message
help:
	@echo "Usage:"
	@echo "  make build          - Build the project"
	@echo "  make docker-build   - Build the Docker image"
	@echo "  make docker-run     - Run the Docker container"
	@echo "  make docker-stop    - Stop and remove the Docker container"
	@echo "  make docker-rmi     - Remove the Docker image"
	@echo "  make clean          - Clean the project"
	@echo "  make help           - Show this help message"

.PHONY: all build docker-build docker-run docker-stop docker-rmi clean help