package com.badasstechie.product.service;

import com.badasstechie.product.grpc.ProductGrpcServiceGrpc;
import com.badasstechie.product.grpc.ProductStock;
import com.badasstechie.product.grpc.ProductStocksRequest;
import com.badasstechie.product.grpc.ProductStocksResponse;
import com.badasstechie.product.model.Product;
import com.badasstechie.product.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductGrpcServiceGrpc.ProductGrpcServiceImplBase {
    private final ProductRepository productRepository;

    @Override
    public void getProductStocks(ProductStocksRequest request, StreamObserver<ProductStocksResponse> responseObserver) {
        List<Product> products = (List<Product>) productRepository.findAllById(request.getIdsList());

        ProductStocksResponse response = products.stream()
                .map(product -> ProductStock.newBuilder().setId(product.getId()).setQuantity(product.getStock()).build())
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        collected -> ProductStocksResponse.newBuilder().addAllStocks(collected).build()));

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
