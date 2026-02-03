using Grpc.Core;
using Order.Grpc;

namespace Order.Services;

public interface IProductGrpcClient
{
    Task<List<ProductInfo>> GetProductDetailsAsync(List<ProductStockInfo> products);
}

public class ProductGrpcClient : IProductGrpcClient
{
    private readonly ProductService.ProductServiceClient _client;

    public ProductGrpcClient(ProductService.ProductServiceClient client)
    {
        _client = client;
    }

    public async Task<List<ProductInfo>> GetProductDetailsAsync(List<ProductStockInfo> products)
    {
        try
        {
            var request = new ProductDetailsRequest();
            request.Products.AddRange(products);

            var response = await _client.GetProductDetailsAsync(request);
            return response.Products.ToList();
        }
        catch (RpcException ex)
        {
            throw new InvalidOperationException($"gRPC call failed: {ex.Status.Detail}", ex);
        }
    }
}
