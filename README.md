<img src="./diagrams/banner.png" alt="eShop logo" title="eShopOnSteroids" align="right" height="60" />

# eShopOnSteroids

[![Build](https://github.com/badass-techie/eShopOnSteroids/actions/workflows/build-and-push-docker-images.yml/badge.svg?branch=main)](https://github.com/badass-techie/eShopOnSteroids/actions/workflows/build-and-push-docker-images.yml) [![Issues](https://img.shields.io/github/issues/badass-techie/eShopOnSteroids)](https://github.com/badass-techie/eShopOnSteroids/issues) [![Pull-Requests](https://img.shields.io/github/issues-pr/badass-techie/eShopOnSteroids)](https://github.com/badass-techie/eShopOnSteroids/pulls) ![Stars](https://img.shields.io/github/stars/badass-techie/eShopOnSteroids) ![Forks](https://img.shields.io/github/forks/badass-techie/eShopOnSteroids)

eShopOnSteroids is a well-architected, distributed, event-driven, cloud-native e-commerce platform powered by the following building blocks of microservices:

1. API Gateway (Spring Cloud Gateway)
2. Service Discovery (Docker and Kubernetes builtin)
3. Distributed Tracing (Sleuth, Zipkin)
4. Circuit Breaker (Resilience4j)
5. Event Bus (RabbitMQ)
6. Database per Microservice (PostgreSQL, MongoDB, Redis)
7. Centralized Monitoring (Prometheus, Grafana)
8. Centralized Logging (Elasticsearch, Fluentd, Kibana)
9. Control Loop (Kubernetes, Terraform)

This code follows best practices such as:

- Unit Testing (JUnit 5, Mockito, Pytest)
- Integration Testing (Testcontainers)
- Design Patterns (Publish/Subscribe, Backend for Frontend, ...)

> microservices, event-driven, distributed systems, e-commerce, domain-driven-design, java, python, spring cloud, spring boot, spring cloud gateway, spring cloud sleuth, zipkin, resilience4j, postgresql, mongodb, redis, cache, rabbitmq, kubernetes, k8s, terraform, observability, prometheus, grafana, elasticsearch, fluentd, kibana

Note: If you are interested in this project, no better way to show it than ★ starring the repository!

## Architecture

The architecture proposes a microservices-oriented implementation where each microservice is responsible for a single business capability. The microservices are deployed in a containerized environment (Docker) and orchestrated by a control loop (Kubernetes) which continuously compares the state of each microservice to the desired state, and takes necessary actions to arrive at the desired state.

Each microservice stores its data in its own database tailored to its requirements, such as an In-Memory Database for a shopping cart whose persistence is short-lived, a Document Database for a product catalog for its flexibility, or a Relational Database for an order management system for its ACID properties.

Microservices communicate externally via REST through a secured API Gateway, and internally via

- gRPC for synchronous communication which excels for its performance
- an event bus for asynchronous communication in which the receiving microservice is free to handle the event whenever it has the capacity

Below is a visual representation:

![Architecture](./diagrams/architecture.png)

- All microservices are inside a private network and not accessible except through the API Gateway.
- The API Gateway routes requests to the corresponding microservice, routes requests to the appropriate endpoint based on the client (Backend for Frontend), and validates the authorization of requests.
- The Identity Microservice acts as an Identity Provider and is responsible for storing users and their roles, and for issuing authorization credentials.
- The Cart Microservice manages the shopping cart of each user. It uses a cache (Redis) as the storage.
- The Product Microservice stores the product catalog and stock. It's subscribed to the Event Bus to receive notifications of new orders and update the stock accordingly.
- The Order Microservice manages order processing and fulfillment. It performs a gRPC call to the Product Microservice to check the availability and pricing of the products in the order pre-checkout and publishes events to the Event Bus to initiate a payment and to update the stock post-checkout.
- The gRPC communication between the microservices is fault-tolerant thanks to a circuit breaker.
- The Payment Microservice handles payment processing. It's subscribed to the Event Bus to receive notifications of new orders and initiate a payment. It does not lie behind the API Gateway as it is not directly accessible by the user. It is also stateless and does not store any data.

Observability services include:

- Zipkin and Sleuth for assigning **traces** to requests to track their path across microservices
![Zipkin Dashboard](./diagrams/zipkin.png)
- Prometheus and Grafana for collecting **metrics** from microservices and setting up alerts for when a metric exceeds a threshold
![Grafana Dashboard](./diagrams/grafana.png)
- Elasticsearch, Fluentd, and Kibana for aggregating **logs** from microservices 
![Kibana Dashboard](./diagrams/kibana.png)

Future work:

- Outsource authentication to a third-party identity provider such as Keycloak

## Setup

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)

Yes, that's it!

### Development

1. Create the env file and fill in the missing values

    ```bash
    cp .env.example .env
    vi .env
    ...
    ```

2. Start the containers

    ```bash
    docker compose -f docker-compose.dev.yml up
    ```

    The first time you run this command, it will take a few minutes to build the images, after which you should be able to access the application at port 8080 locally. Changes to the source code will be automatically reflected in the containers without any extra steps.

    To stop the containers, run:

    ```bash
    docker compose -f docker-compose.dev.yml down
    ```

    To remove saved data along with the containers, run the following command:

    ```bash
    docker compose -f docker-compose.dev.yml down -v
    ```

### Production

#### Deploy containers with docker compose

1. Create the env file and fill in the missing values

    ```bash
    cp .env.example .env
    vi .env
    ...
    ```

    Then:

2. (Optional) Run the following command to build the images locally:

    ```bash
    docker compose build
    ```

    It will take a few minutes. Alternatively, you can skip this step and the images will be pulled from Docker Hub.

3. Start the containers

    ```bash
    docker compose up
    ```

You can now access the application at port 8080 locally

#### Deploy to local Kubernetes cluster

1. Ensure you have enabled Kubernetes in Docker Desktop as below:

    ![Enable Kubernetes](./diagrams/docker-desktop-kubernetes.png)

    (or alternatively, install [Minikube](https://minikube.sigs.k8s.io/docs/start/) and start it with `minikube start`)

    Then:

2. Enter the directory containing the Kubernetes manifests

    ```bash
    cd k8s
    ```

3. Create the env file and fill in the missing values

    ```bash
    cp ./config/.env.example ./config/.env
    vi ./config/.env
    ...
    ```

4. Create the namespace

    ```bash
    kubectl apply -f ./namespace
    ```

5. Change the context to the namespace

    ```bash
    kubectl config set-context --current --namespace=eshoponsteroids
    ```

6. Create Kubernetes secrets from the env file

    ```bash
    kubectl create secret generic env-secrets --from-env-file=./config/.env
    ```

7. Apply the configmaps

    ```bash
    kubectl apply -f ./config
    ```

8. Apply the persistent volumes

    ```bash
    kubectl apply -f ./volumes
    ```

9. Install kubernetes metrics server (needed to scale microservices based on metrics)

    ```bash
    kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
    ```

10. Deploy the containers

    ```bash
    kubectl apply -f ./deployments
    ```

11. Expose the API gateway

    ```bash
    kubectl apply -f ./networking/node-port.yml
    ```

You can now access the application at port 30080 locally

To tear everything down, run the following command:

```bash
kubectl delete namespace eshoponsteroids
```

Future work:

- Simplify the deployment process by templating similar manifests with Helm
- Overlay/patch manifests to tailor them to different environments (dev, staging, prod, ...) using Kustomize

#### Deploy to AWS EKS cluster

Typically, cloud engineers provision cloud resources, and developers focus more on shipping their code to these resources. However, as a developer, to be able to design cloud-native applications such as this one, it's important to understand the infrastructure on which your code runs (hence the rise of DevOps as a software development methodology). That is why we will provision our own Kubernetes cluster on AWS EKS (Elastic Kubernetes Service) for our application.

For this section, in addition to Docker you will need:

- Basic knowledge of the AWS platform
- An AWS account
- AWS CLI configured with the credentials of either your account or an IAM user with administrator access (run `aws configure`)

Here is a breakdown of the resources we will provision:

- VPC (Virtual Private Cloud): a virtual network where our cluster will reside
- Subnets: 2 public and 2 private subnets in different availability zones (required by EKS to ensure high availability of the cluster)
- Internet Gateway: allows external access to our VPC
- NAT Gateway: allows our private subnets to access the internet
- Security Groups: for controlling inbound and outbound traffic to our cluster
- IAM Roles: for granting permissions to our cluster to access other AWS services and perform actions on our behalf
- EKS Cluster: the Kubernetes cluster itself
- EKS Node Group: the worker nodes that will run our containers

To provision the cluster, we will use Terraform as opposed to AWS Console or eksctl. [What is Terraform and how does it simplify infrastructure management?](https://www.ibm.com/topics/terraform) All the above resources are already defined as terraform manifests and what is left to do is to apply them to our AWS account.

Thus far we have applied the approach used by Terraform to create and manage resources (called a declarative approach) in Docker Compose and Kubernetes. Its main advantage is that it is reusable and always yields the same results.

Let us start by installing Terraform (if you haven't already):

Windows (20H2 or later):

```bash
winget install HashiCorp.Terraform
```

Mac:

```bash
brew tap hashicorp/tap
brew install hashicorp/tap/terraform
```

Linux:

```bash
curl -O https://releases.hashicorp.com/terraform/1.6.2/terraform_1.6.2_linux_amd64.zip
unzip terraform_*_linux_amd64.zip
sudo mv terraform /usr/local/bin/
terraform -v    # verify installation
```

Then:

1. Enter the directory containing the terraform manifests

    ```bash
    cd terraform
    ```

2. Initialize Terraform

    ```bash
    terraform init
    ```

    This step downloads the required providers (AWS, Kubernetes, Helm, ...) and plugins.

3. Generate an execution plan to see what resources will be created

    ```bash
    terraform plan -out=tfplan
    ```

4. Apply the execution plan

    ```bash
    terraform apply tfplan
    ```

    This step will take approximately 15 minutes.

5. Configure kubectl to connect to the cluster

    ```bash
    aws eks update-kubeconfig --region us-east-1 --name eshoponsteroids
    ```

6. Verify that the cluster is up and running

    ```bash
    kubectl get nodes
    ```

    The output should be similar to:

    ```bash
    NAME                                      STATUS   ROLES    AGE   VERSION
    ip-10-0-8-72.us-east-1.compute.internal   Ready    <none>   10m   v1.28.1-eks-55daa9d
    ```

Let us now deploy our application to the cluster:

1. Go back to the root directory of the project

    ```bash
    cd ..
    ```

2. Execute steps 2 to 10 of [Deploy to local Kubernetes cluster](#deploy-to-local-kubernetes-cluster).

3. Run `kubectl get deployments --watch` and `kubectl get statefulsets --watch` to monitor the progress.

4. Request a load balancer from AWS to expose the application's API gateway outside the cluster once deployments are ready

    ```bash
    kubectl apply -f ./networking/load-balancer.yml
    ```

    Run `kubectl describe service load-balancer | grep Ingress` to get the load balancer's address. It should be similar to:

    ```bash
    LoadBalancer Ingress:     a9611669cf885464a8fac52687bbbba6-690733611.us-east-1.elb.amazonaws.com
    ```

    Go to the AWS EC2 Console's Load Balancer feature and verify that the load balancer has been created. ![Elastic Load Balancer](./diagrams/aws-elb.png)

You can now access the application at port 8080 with the hostname as the load balancer's address. You can also access the observability services with their respective ports.

To tear down the infrastructure, run the following commands:

```bash
kubectl delete namespace eshoponsteroids
terraform destroy
```

Remember to Change the context of kubectl back to your local cluster if you have one:

```bash
kubectl config get-contexts
kubectl config delete-context [name-of-remote-cluster]
kubectl config use-context [name-of-local-cluster]
```

Future work:

- Use an Ingress Controller (e.g. Nginx) to expose the API Gateway outside the cluster, or as the API Gateway itself.

## Usage

The interface (a Single-Page Application) is still a work in progress, but the available endpoints can be found in the API documentation which post-deployment can be accessed at:

- http://[host]:[port]/api/v1/[name-of-microservice]/swagger-ui.html

![API Documentation](./diagrams/swagger.png)

## Testing

First, enter the container of the microservice you want to test:

```bash
docker exec -it [container-id] bash
```

For example:

```bash
docker exec -it eshoponsteroids-cart-1 bash
```

### Unit tests

To run Java unit tests, run the following command:

```bash
mvn test
```

To run Python unit tests, run the following command:

```bash
pytest
```

### Integration tests

- Make sure you have Docker installed and running
- Run the following command in your local system (not the containers):

```bash
mvnw verify
```

## Third Party Integrations

- [Stripe](https://stripe.com/) for credit and debit card payments
- [Daraja](https://developer.safaricom.co.ke/) for M-Pesa payments (M-Pesa is a mobile money service in Kenya)
- [Africa's Talking](https://africastalking.com/) for SMS notifications (WIP)
