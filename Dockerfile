# Use maven as base image
FROM maven:3.8.3-openjdk-17

# Copy the entire maven project
COPY . /build/

# Set working directory
WORKDIR /build

# Generate the rsa keys
RUN microdnf install -y openssl
RUN openssl genrsa -out keypair.pem 2048
RUN openssl rsa -in keypair.pem -pubout -out public.pem
RUN openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem

# Copy the private key and public key to identity microservice to sign JWT
RUN mkdir -p /build/Identity/src/main/resources/certs
RUN cp private.pem /build/Identity/src/main/resources/certs/private.pem
RUN cp public.pem /build/Identity/src/main/resources/certs/public.pem

# Copy the public key to api gateway to verify JWT
RUN mkdir -p /build/ApiGateway/src/main/resources/certs
RUN cp public.pem /build/ApiGateway/src/main/resources/certs/public.pem

# Overwrite application.yml with application-docker.yml for each maven module
RUN cp ApiGateway/src/main/resources/application-docker.yml ApiGateway/src/main/resources/application.yml
RUN cp Cart/src/main/resources/application-docker.yml Cart/src/main/resources/application.yml
RUN cp DiscoveryServer/src/main/resources/application-docker.yml DiscoveryServer/src/main/resources/application.yml
RUN cp Identity/src/main/resources/application-docker.yml Identity/src/main/resources/application.yml
RUN cp Order/src/main/resources/application-docker.yml Order/src/main/resources/application.yml
RUN cp Product/src/main/resources/application-docker.yml Product/src/main/resources/application.yml

# Build the project
RUN mvn clean package  -DskipTests


# specify targets for each maven module
# ApiGateway
# Use lightweight jre as base image
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl AS api-gateway

# Copy only the jar file
COPY --from=0 /build/ApiGateway/target/apigateway-1.0-SNAPSHOT.jar apigateway.jar

# Run the jar file
ENTRYPOINT ["java","-jar","apigateway.jar"]


# DiscoveryServer
# Use lightweight jre as base image
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl AS discovery-server

# Copy only the jar file
COPY --from=0 /build/DiscoveryServer/target/discoveryserver-1.0-SNAPSHOT.jar discoveryserver.jar

# Run the jar file
ENTRYPOINT ["java","-jar","discoveryserver.jar"]


# Cart
# Use lightweight jre as base image
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl AS cart

# Copy only the jar file
COPY --from=0 /build/Cart/target/cart-1.0-SNAPSHOT.jar cart.jar

# Run the jar file
ENTRYPOINT ["java","-jar","cart.jar"]


# Identity
# Use lightweight jre as base image
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl AS identity

# Copy only the jar file
COPY --from=0 /build/Identity/target/identity-1.0-SNAPSHOT.jar identity.jar

# Run the jar file
ENTRYPOINT ["java","-jar","identity.jar"]


# Order
# Use lightweight jre as base image
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl AS order

# Copy only the jar file
COPY --from=0 /build/Order/target/order-1.0-SNAPSHOT.jar order.jar

# Run the jar file
ENTRYPOINT ["java","-jar","order.jar"]


# Product
# Use lightweight jre as base image
FROM bellsoft/liberica-runtime-container:jre-17-slim-musl AS product

# Copy only the jar file
COPY --from=0 /build/Product/target/product-1.0-SNAPSHOT.jar product.jar

# Run the jar file
ENTRYPOINT ["java","-jar","product.jar"]
