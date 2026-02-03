using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.ComponentModel.DataAnnotations;

namespace Product.Models;

public class Product
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [Required]
    public string Name { get; set; } = string.Empty;

    public string? Description { get; set; }

    [Required]
    [Range(0.01, double.MaxValue)]
    public decimal Price { get; set; }

    public string? Image { get; set; }

    [Required]
    [Range(0, int.MaxValue)]
    public int Stock { get; set; }

    public string? BrandId { get; set; }

    public string? Category { get; set; }

    public DateTime Created { get; set; } = DateTime.UtcNow;

    public DateTime Updated { get; set; } = DateTime.UtcNow;
}

public class Brand
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [Required]
    public string Name { get; set; } = string.Empty;

    public string? Description { get; set; }

    public string? Logo { get; set; }

    public DateTime Created { get; set; } = DateTime.UtcNow;
}
