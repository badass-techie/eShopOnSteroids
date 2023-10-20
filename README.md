<img src="./diagrams/banner.png" alt="eShop logo" title="eShopOnSteroids" align="right" height="60" />

# eShopOnSteroids

[![Build](https://github.com/badass-techie/eShopOnSteroids/actions/workflows/build-and-push-docker-images.yml/badge.svg?branch=main)](https://github.com/badass-techie/eShopOnSteroids/actions/workflows/build-and-push-docker-images.yml) [![Issues](https://img.shields.io/github/issues/badass-techie/eShopOnSteroids)](https://github.com/badass-techie/eShopOnSteroids/issues) [![Pull-Requests](https://img.shields.io/github/issues-pr/badass-techie/eShopOnSteroids)](https://github.com/badass-techie/eShopOnSteroids/pulls) ![Stars](https://img.shields.io/github/stars/badass-techie/eShopOnSteroids) ![Forks](https://img.shields.io/github/forks/badass-techie/eShopOnSteroids)

eShopOnSteroids is a well-architected, distributed, event-driven, domain-driven, server-side e-commerce application powered by the following building blocks of microservices:

1. API Gateway (Spring Cloud Gateway)
2. Service Discovery (HashiCorp Consul)
3. Distributed Tracing (Sleuth, Zipkin)
4. Circuit Breaker (Resilience4j)
5. Message Bus (RabbitMQ)
6. Database per Microservice (PostgreSQL, MongoDB, Redis)
7. Centralized Monitoring (Prometheus, Grafana)
8. Control Loop (Kubernetes, Terraform)

This code follows best practices such as:

- Unit Testing (JUnit 5, Mockito)
- Integration Testing (Testcontainers)
- Design Patterns (Builder, Singleton, PubSub, ...)

> microservices, event-driven, distributed systems, e-commerce, domain-driven-design, spring cloud, spring boot, spring cloud gateway, hashicorp consul, hashicorp vault, spring cloud sleuth, zipkin, resilience4j, postgresql, mongodb, redis, cache, kubernetes, k8s, prometheus, grafana, rabbitmq, terraform

## Architecture

The architecture proposes a microservices oriented implementation where each microservice is responsible for a single business capability. The microservices are deployed in a containerized environment (Docker) and managed by a control loop (Kubernetes) which compares the state of each microservice to the desired state, and takes necessary actions to eventually arrive at the desired state.

Each microservice stores its data in its own database tailored to its requirements, such as an In-Memory Database for a shopping cart whose persistence is short-lived, a Document Database for a product catalog for its flexibility, or a Relational Database for an order management system for its ACID properties.

Microservices communicate externally via REST through a secured API Gateway, and internally via

- gRPC for synchronous communication which excels for its performance
- a message bus for asynchronous communication in which the receiving microservice is free to handle the message whenever it has the capacity

Below is a visual representation:

![Architecture](./diagrams/architecture.png)

- All microservices are inside a private network and not accessible except through the API Gateway.
- The API Gateway routes requests to the appropriate microservice, and validates the authorization of requests to all microservices except the Identity Microservice.
- The Identity Microservice acts as an Identity Issuer and is responsible for storing users and their roles, and for issuing authorization credentials.
- All microservices send regular heartbeats to the Discovery Server which helps them locate each other as they may have multiple instances running hence different IP addresses.
- The Cart Microservice manages the shopping cart of each user. It uses a cache (Redis) as the storage.
- The Product Microservice stores the product catalog and stock. It's subscribed to the Message Bus to receive notifications of new orders and update the stock accordingly.
- The Order Microservice manages order processing and fulfillment. It performs a gRPC call to the Product Microservice to check the availability of the products in the order pre-checkout, and publishes a message to the Message Bus when an order is placed successfully.
- The gRPC communication between the microservices is fault-tolerant and resilient to transient failures thanks to Resilience4j Circuit Breaker.

Admin services include:

- Consul dashboard to monitor the availability and health of microservices
![Consul Dashboard](./diagrams/consul.png)
- Zipkin dashboard for tracing requests across microservices
![Zipkin Dashboard](./diagrams/zipkin.png)
- Grafana dashboard for visualizing the metrics of microservices and setting up alerts for when a metric exceeds a threshold
![Grafana Dashboard](./diagrams/grafana.png)

## Setup

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)

### Basic Scenario

#### Deploy containers with docker compose

1. (Optional) Run the following command to build the images locally:

```bash
docker compose build
```

It will take a few minutes. Alternatively, you can skip this step and the images will be pulled from Docker Hub.

Then:

2. Create the env file and fill in the missing values

```bash
cp .env.example .env
vi .env
...
```

3. Start the containers

```bash
docker compose up
```

You can now access the application at port 8080 locally

### Advanced Scenarios

#### Deploy to local Kubernetes cluster

1. Ensure you have enabled Kubernetes in Docker Desktop as below:

![Enable Kubernetes](./diagrams/docker-desktop-kubernetes.png)

(or alternatively, install [Minikube](https://minikube.sigs.k8s.io/docs/start/) and start it with `minikube start`)

Then:

2. Create the env file and fill in the missing values

```bash
cd k8s
cp ./config/.env.example ./config/.env
vi ./config/.env
...
```

3. Create the namespace

```bash
kubectl apply -f ./namespace
```

4. Change the context to the namespace

```bash
kubectl config set-context --current --namespace=eshoponsteroids
```

4. Create Kubernetes secret from the env file

```bash
kubectl create secret generic env-secrets --from-env-file=./config/.env --namespace=eshoponsteroids
```

5. Apply the configs

```bash
kubectl apply -f ./config
```

6. Create the persistent volumes

```bash
kubectl apply -f ./volumes
```

7. Install kubernetes metrics server (needed to scale microservices based on metrics)

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

8. Deploy the containers

```bash
kubectl apply -f ./deployments
```

9. Expose the API gateway and admin services

```bash
kubectl apply -f ./networking/node-port.yml
```

You can now access the application at port 30080 locally

Future work:

- Simplify the deployment process by using Helm charts
- Customize configs with Kustomize

#### Deploy to AWS EKS cluster

Coming soon...

## Usage

The interface (a Single-Page Application) is still a work in progress, but the available endpoints can be found in the API documentation which post-deployment can be accessed at:

- http://[host]:[port]/api/v1/[name-of-microservice]/swagger-ui.html

![API Documentation](./diagrams/swagger.png)

## Testing

### Prerequisites

- Java 17+
- Docker

### Unit tests

To run the unit tests, run the following command:

```bash
mvnw test
```

### Integration tests

- Make sure you have Docker installed and running
- Run the following command to start the testcontainers:

```bash
mvnw verify
```
