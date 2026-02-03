using Order.Models;
using System.ComponentModel.DataAnnotations;

namespace Order.DTOs;

public record OrderItemRequest(
    [Required] string ProductId,
    [Required] [Range(1, int.MaxValue)] int Quantity
);

public record OrderItemResponse(
    long Id,
    string ProductId,
    string ProductName,
    decimal UnitPrice,
    int Quantity
);

public record OrderRequest(
    [Required] long UserId,
    [Required] List<OrderItemRequest> Items,
    [Required] string DeliveryAddress
);

public record OrderResponse(
    long Id,
    long UserId,
    string OrderNumber,
    List<OrderItemResponse> Items,
    OrderStatus Status,
    string DeliveryAddress,
    DateTime Created
);

public record OrderPaymentRequest(
    string OrderId,
    decimal Amount,
    string PaymentMethod
);

public record OrderPaymentResponse(
    string OrderId,
    bool Success,
    string Message
);
