using MongoDB.Driver;
using Product.DTOs;
using Product.Models;

namespace Product.Services;

public interface IProductService
{
    Task<ProductResponse> CreateProductAsync(ProductRequest request);
    Task<ProductResponse?> GetProductByIdAsync(string id);
    Task<PaginatedResponse<ProductResponse>> GetAllProductsAsync(int page, int pageSize, string? search, string? category, string? brandId);
    Task<ProductResponse?> UpdateProductAsync(string id, ProductRequest request);
    Task<bool> DeleteProductAsync(string id);
    Task<bool> UpdateStockAsync(string id, int quantity);
    Task<List<Models.Product>> GetProductsByIdsAsync(List<string> ids);
}

public class ProductService : IProductService
{
    private readonly IMongoCollection<Models.Product> _products;

    public ProductService(IMongoDatabase database)
    {
        _products = database.GetCollection<Models.Product>("products");
    }

    public async Task<ProductResponse> CreateProductAsync(ProductRequest request)
    {
        var product = new Models.Product
        {
            Name = request.Name,
            Description = request.Description,
            Price = request.Price,
            Image = request.Image,
            Stock = request.Stock,
            BrandId = request.BrandId,
            Category = request.Category,
            Created = DateTime.UtcNow,
            Updated = DateTime.UtcNow
        };

        await _products.InsertOneAsync(product);
        return MapToResponse(product);
    }

    public async Task<ProductResponse?> GetProductByIdAsync(string id)
    {
        var product = await _products.Find(p => p.Id == id).FirstOrDefaultAsync();
        return product != null ? MapToResponse(product) : null;
    }

    public async Task<PaginatedResponse<ProductResponse>> GetAllProductsAsync(
        int page, int pageSize, string? search, string? category, string? brandId)
    {
        var filterBuilder = Builders<Models.Product>.Filter;
        var filter = filterBuilder.Empty;

        if (!string.IsNullOrEmpty(search))
        {
            filter &= filterBuilder.Regex(p => p.Name, new MongoDB.Bson.BsonRegularExpression(search, "i"));
        }

        if (!string.IsNullOrEmpty(category))
        {
            filter &= filterBuilder.Eq(p => p.Category, category);
        }

        if (!string.IsNullOrEmpty(brandId))
        {
            filter &= filterBuilder.Eq(p => p.BrandId, brandId);
        }

        var totalCount = await _products.CountDocumentsAsync(filter);
        var products = await _products.Find(filter)
            .Skip((page - 1) * pageSize)
            .Limit(pageSize)
            .ToListAsync();

        return new PaginatedResponse<ProductResponse>(
            products.Select(MapToResponse).ToList(),
            page,
            pageSize,
            totalCount
        );
    }

    public async Task<ProductResponse?> UpdateProductAsync(string id, ProductRequest request)
    {
        var update = Builders<Models.Product>.Update
            .Set(p => p.Name, request.Name)
            .Set(p => p.Description, request.Description)
            .Set(p => p.Price, request.Price)
            .Set(p => p.Image, request.Image)
            .Set(p => p.Stock, request.Stock)
            .Set(p => p.BrandId, request.BrandId)
            .Set(p => p.Category, request.Category)
            .Set(p => p.Updated, DateTime.UtcNow);

        var product = await _products.FindOneAndUpdateAsync(
            p => p.Id == id,
            update,
            new FindOneAndUpdateOptions<Models.Product> { ReturnDocument = ReturnDocument.After }
        );

        return product != null ? MapToResponse(product) : null;
    }

    public async Task<bool> DeleteProductAsync(string id)
    {
        var result = await _products.DeleteOneAsync(p => p.Id == id);
        return result.DeletedCount > 0;
    }

    public async Task<bool> UpdateStockAsync(string id, int quantity)
    {
        var update = Builders<Models.Product>.Update
            .Inc(p => p.Stock, quantity)
            .Set(p => p.Updated, DateTime.UtcNow);

        var result = await _products.UpdateOneAsync(p => p.Id == id, update);
        return result.ModifiedCount > 0;
    }

    public async Task<List<Models.Product>> GetProductsByIdsAsync(List<string> ids)
    {
        return await _products.Find(p => ids.Contains(p.Id!)).ToListAsync();
    }

    private static ProductResponse MapToResponse(Models.Product product)
    {
        return new ProductResponse(
            product.Id!,
            product.Name,
            product.Description,
            product.Price,
            product.Image,
            product.Stock,
            product.BrandId,
            product.Category,
            product.Created,
            product.Updated
        );
    }
}
