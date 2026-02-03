using Microsoft.EntityFrameworkCore;
using Order.Data;
using Order.DTOs;
using Order.Grpc;
using Order.Models;

namespace Order.Services;

public interface IOrderService
{
    Task<OrderResponse> CreateOrderAsync(OrderRequest request);
    Task<OrderResponse?> GetOrderByIdAsync(long id);
    Task<List<OrderResponse>> GetOrdersByUserIdAsync(long userId);
    Task<OrderResponse?> UpdateOrderStatusAsync(long id, OrderStatus status);
    Task<bool> DeleteOrderAsync(long id);
}

public class OrderService : IOrderService
{
    private readonly OrderDbContext _context;
    private readonly IProductGrpcClient _productGrpcClient;
    private readonly IRabbitMQService _rabbitMQService;

    public OrderService(
        OrderDbContext context,
        IProductGrpcClient productGrpcClient,
        IRabbitMQService rabbitMQService)
    {
        _context = context;
        _productGrpcClient = productGrpcClient;
        _rabbitMQService = rabbitMQService;
    }

    public async Task<OrderResponse> CreateOrderAsync(OrderRequest request)
    {
        // Verify product availability and get details via gRPC
        var productStockInfo = request.Items.Select(i => new ProductStockInfo
        {
            Id = i.ProductId,
            Quantity = i.Quantity
        }).ToList();

        var productDetails = await _productGrpcClient.GetProductDetailsAsync(productStockInfo);

        if (productDetails.Count != request.Items.Count)
        {
            throw new InvalidOperationException("Some products are not available");
        }

        // Create order
        var order = new Models.Order
        {
            UserId = request.UserId,
            OrderNumber = GenerateOrderNumber(),
            DeliveryAddress = request.DeliveryAddress,
            Status = OrderStatus.PENDING,
            Created = DateTime.UtcNow,
            Items = new List<OrderItem>()
        };

        foreach (var item in request.Items)
        {
            var product = productDetails.First(p => p.Id == item.ProductId);
            order.Items.Add(new OrderItem
            {
                ProductId = item.ProductId,
                ProductName = product.Name,
                UnitPrice = (decimal)product.Price,
                Quantity = item.Quantity
            });
        }

        _context.Orders.Add(order);
        await _context.SaveChangesAsync();

        // Publish payment request to RabbitMQ
        var paymentRequest = new OrderPaymentRequest(
            order.OrderNumber,
            order.Items.Sum(i => i.UnitPrice * i.Quantity),
            "CARD"
        );
        _rabbitMQService.PublishMessage("payment_queue", paymentRequest);

        // Publish stock update to RabbitMQ
        var stockUpdate = request.Items.Select(i => new
        {
            ProductId = i.ProductId,
            Quantity = -i.Quantity // Negative to decrease stock
        }).ToList();
        _rabbitMQService.PublishMessage("stock_queue", stockUpdate);

        return MapToResponse(order);
    }

    public async Task<OrderResponse?> GetOrderByIdAsync(long id)
    {
        var order = await _context.Orders
            .Include(o => o.Items)
            .FirstOrDefaultAsync(o => o.Id == id);

        return order != null ? MapToResponse(order) : null;
    }

    public async Task<List<OrderResponse>> GetOrdersByUserIdAsync(long userId)
    {
        var orders = await _context.Orders
            .Include(o => o.Items)
            .Where(o => o.UserId == userId)
            .ToListAsync();

        return orders.Select(MapToResponse).ToList();
    }

    public async Task<OrderResponse?> UpdateOrderStatusAsync(long id, OrderStatus status)
    {
        var order = await _context.Orders
            .Include(o => o.Items)
            .FirstOrDefaultAsync(o => o.Id == id);

        if (order == null) return null;

        order.Status = status;
        await _context.SaveChangesAsync();

        return MapToResponse(order);
    }

    public async Task<bool> DeleteOrderAsync(long id)
    {
        var order = await _context.Orders.FindAsync(id);
        if (order == null) return false;

        _context.Orders.Remove(order);
        await _context.SaveChangesAsync();
        return true;
    }

    private static string GenerateOrderNumber()
    {
        return $"ORD-{DateTime.UtcNow:yyyyMMdd}-{Guid.NewGuid().ToString()[..8].ToUpper()}";
    }

    private static OrderResponse MapToResponse(Models.Order order)
    {
        return new OrderResponse(
            order.Id,
            order.UserId,
            order.OrderNumber,
            order.Items.Select(i => new OrderItemResponse(
                i.Id,
                i.ProductId,
                i.ProductName,
                i.UnitPrice,
                i.Quantity
            )).ToList(),
            order.Status,
            order.DeliveryAddress,
            order.Created
        );
    }
}
