using System.ComponentModel.DataAnnotations;

namespace Cart.DTOs;

public record CartItemRequest(
    [Required] long ProductId,
    [Required] [Range(1, int.MaxValue)] int Quantity
);

public record CartItemResponse(
    long ProductId,
    int Quantity
);

public record CartResponse(
    string UserId,
    List<CartItemResponse> Items,
    DateTime LastModified
);
