using System.ComponentModel.DataAnnotations;

namespace Product.DTOs;

public record ProductRequest(
    [Required] string Name,
    string? Description,
    [Required] [Range(0.01, double.MaxValue)] decimal Price,
    string? Image,
    [Required] [Range(0, int.MaxValue)] int Stock,
    string? BrandId,
    string? Category
);

public record ProductResponse(
    string Id,
    string Name,
    string? Description,
    decimal Price,
    string? Image,
    int Stock,
    string? BrandId,
    string? Category,
    DateTime Created,
    DateTime Updated
);

public record BrandRequest(
    [Required] string Name,
    string? Description,
    string? Logo
);

public record BrandResponse(
    string Id,
    string Name,
    string? Description,
    string? Logo,
    DateTime Created
);

public record PaginatedResponse<T>(
    List<T> Items,
    int Page,
    int PageSize,
    long TotalCount
);
