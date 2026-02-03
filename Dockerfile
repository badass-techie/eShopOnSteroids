# Build stage for .NET services
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Generate RSA keys for JWT
RUN apt-get update && apt-get install -y openssl && \
    openssl genrsa -out /keypair.pem 2048 && \
    openssl rsa -in /keypair.pem -pubout -out /public.pem && \
    openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in /keypair.pem -out /private.pem

# Copy solution and project files
COPY ["eShopOnSteroids.sln", "./"]
COPY ["Identity/Identity.csproj", "Identity/"]
COPY ["Cart/Cart.csproj", "Cart/"]
COPY ["Order/Order.csproj", "Order/"]
COPY ["Product/Product.csproj", "Product/"]
COPY ["ApiGateway/ApiGateway.csproj", "ApiGateway/"]

# Restore dependencies
RUN dotnet restore "eShopOnSteroids.sln"

# Copy source code
COPY . .

# Create certs directory and copy keys
RUN mkdir -p /src/Identity/certs && \
    cp /private.pem /src/Identity/certs/private.pem && \
    cp /public.pem /src/Identity/certs/public.pem && \
    mkdir -p /src/ApiGateway/certs && \
    cp /public.pem /src/ApiGateway/certs/public.pem

# ApiGateway
FROM build AS api-gateway-build
WORKDIR /src/ApiGateway
RUN dotnet publish "ApiGateway.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS api-gateway
WORKDIR /app
COPY --from=api-gateway-build /app/publish .
COPY --from=build /src/ApiGateway/certs ./certs
ENTRYPOINT ["dotnet", "ApiGateway.dll"]

# Cart
FROM build AS cart-build
WORKDIR /src/Cart
RUN dotnet publish "Cart.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS cart
WORKDIR /app
COPY --from=cart-build /app/publish .
ENTRYPOINT ["dotnet", "Cart.dll"]

# Identity
FROM build AS identity-build
WORKDIR /src/Identity
RUN dotnet publish "Identity.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS identity
WORKDIR /app
COPY --from=identity-build /app/publish .
COPY --from=build /src/Identity/certs ./certs
ENTRYPOINT ["dotnet", "Identity.dll"]

# Order
FROM build AS order-build
WORKDIR /src/Order
RUN dotnet publish "Order.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS order
WORKDIR /app
COPY --from=order-build /app/publish .
ENTRYPOINT ["dotnet", "Order.dll"]

# Product
FROM build AS product-build
WORKDIR /src/Product
RUN dotnet publish "Product.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS product
WORKDIR /app
COPY --from=product-build /app/publish .
ENTRYPOINT ["dotnet", "Product.dll"]
