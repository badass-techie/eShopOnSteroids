using Grpc.Core;
using Product.Grpc;

namespace Product.Services;

public class ProductGrpcService : ProductService.ProductServiceBase
{
    private readonly IProductService _productService;
    private readonly ILogger<ProductGrpcService> _logger;

    public ProductGrpcService(IProductService productService, ILogger<ProductGrpcService> logger)
    {
        _productService = productService;
        _logger = logger;
    }

    public override async Task<ProductDetailsResponse> GetProductDetails(
        ProductDetailsRequest request, ServerCallContext context)
    {
        try
        {
            var productIds = request.Products.Select(p => p.Id).ToList();
            var products = await _productService.GetProductsByIdsAsync(productIds);

            var response = new ProductDetailsResponse();

            foreach (var product in products)
            {
                var requestedProduct = request.Products.FirstOrDefault(p => p.Id == product.Id);
                if (requestedProduct != null && product.Stock >= requestedProduct.Quantity)
                {
                    response.Products.Add(new ProductInfo
                    {
                        Id = product.Id!,
                        Name = product.Name,
                        Price = (double)product.Price,
                        Stock = product.Stock
                    });
                }
            }

            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error getting product details");
            throw new RpcException(new Status(StatusCode.Internal, "Error getting product details"));
        }
    }
}
