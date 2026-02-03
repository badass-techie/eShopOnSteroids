using MongoDB.Driver;
using Product.DTOs;
using Product.Models;

namespace Product.Services;

public interface IBrandService
{
    Task<BrandResponse> CreateBrandAsync(BrandRequest request);
    Task<BrandResponse?> GetBrandByIdAsync(string id);
    Task<List<BrandResponse>> GetAllBrandsAsync();
    Task<BrandResponse?> UpdateBrandAsync(string id, BrandRequest request);
    Task<bool> DeleteBrandAsync(string id);
}

public class BrandService : IBrandService
{
    private readonly IMongoCollection<Brand> _brands;

    public BrandService(IMongoDatabase database)
    {
        _brands = database.GetCollection<Brand>("brands");
    }

    public async Task<BrandResponse> CreateBrandAsync(BrandRequest request)
    {
        var brand = new Brand
        {
            Name = request.Name,
            Description = request.Description,
            Logo = request.Logo,
            Created = DateTime.UtcNow
        };

        await _brands.InsertOneAsync(brand);
        return MapToResponse(brand);
    }

    public async Task<BrandResponse?> GetBrandByIdAsync(string id)
    {
        var brand = await _brands.Find(b => b.Id == id).FirstOrDefaultAsync();
        return brand != null ? MapToResponse(brand) : null;
    }

    public async Task<List<BrandResponse>> GetAllBrandsAsync()
    {
        var brands = await _brands.Find(_ => true).ToListAsync();
        return brands.Select(MapToResponse).ToList();
    }

    public async Task<BrandResponse?> UpdateBrandAsync(string id, BrandRequest request)
    {
        var update = Builders<Brand>.Update
            .Set(b => b.Name, request.Name)
            .Set(b => b.Description, request.Description)
            .Set(b => b.Logo, request.Logo);

        var brand = await _brands.FindOneAndUpdateAsync(
            b => b.Id == id,
            update,
            new FindOneAndUpdateOptions<Brand> { ReturnDocument = ReturnDocument.After }
        );

        return brand != null ? MapToResponse(brand) : null;
    }

    public async Task<bool> DeleteBrandAsync(string id)
    {
        var result = await _brands.DeleteOneAsync(b => b.Id == id);
        return result.DeletedCount > 0;
    }

    private static BrandResponse MapToResponse(Brand brand)
    {
        return new BrandResponse(
            brand.Id!,
            brand.Name,
            brand.Description,
            brand.Logo,
            brand.Created
        );
    }
}
