package com.badasstechie.product.service;

import com.badasstechie.product.grpc.ProductGrpcServiceGrpc;
import com.badasstechie.product.grpc.ProductDetails;
import com.badasstechie.product.grpc.ProductDetailsRequest;
import com.badasstechie.product.grpc.ProductDetailsResponse;
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
    public void getProductDetails(ProductDetailsRequest request, StreamObserver<ProductDetailsResponse> responseObserver) {
        List<Product> products = (List<Product>) productRepository.findAllById(request.getIdsList());

        ProductDetailsResponse response = products.stream()
                .map(product ->
                        ProductDetails
                                .newBuilder()
                                .setId(product.getId())
                                .setName(product.getName())
                                .setPrice(product.getPrice().intValue())
                                .setStock(product.getStock())
                                .build()
                )
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        collected -> ProductDetailsResponse.newBuilder().addAllProductDetails(collected).build()));

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
