using Microsoft.AspNetCore.Mvc;
using Order.DTOs;
using Order.Models;
using Order.Services;

namespace Order.Controllers;

[ApiController]
[Route("api/v1/order")]
public class OrderController : ControllerBase
{
    private readonly IOrderService _orderService;

    public OrderController(IOrderService orderService)
    {
        _orderService = orderService;
    }

    /// <summary>
    /// Create a new order
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<OrderResponse>> CreateOrder([FromBody] OrderRequest request)
    {
        try
        {
            var response = await _orderService.CreateOrderAsync(request);
            return CreatedAtAction(nameof(GetOrderById), new { id = response.Id }, response);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Get order by ID
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<OrderResponse>> GetOrderById(long id)
    {
        var order = await _orderService.GetOrderByIdAsync(id);
        if (order == null)
        {
            return NotFound(new { message = "Order not found" });
        }
        return Ok(order);
    }

    /// <summary>
    /// Get orders by user ID
    /// </summary>
    [HttpGet("user/{userId}")]
    public async Task<ActionResult<List<OrderResponse>>> GetOrdersByUserId(long userId)
    {
        var orders = await _orderService.GetOrdersByUserIdAsync(userId);
        return Ok(orders);
    }

    /// <summary>
    /// Update order status
    /// </summary>
    [HttpPatch("{id}/status")]
    public async Task<ActionResult<OrderResponse>> UpdateOrderStatus(long id, [FromBody] OrderStatus status)
    {
        var order = await _orderService.UpdateOrderStatusAsync(id, status);
        if (order == null)
        {
            return NotFound(new { message = "Order not found" });
        }
        return Ok(order);
    }

    /// <summary>
    /// Delete order
    /// </summary>
    [HttpDelete("{id}")]
    public async Task<ActionResult> DeleteOrder(long id)
    {
        var deleted = await _orderService.DeleteOrderAsync(id);
        if (!deleted)
        {
            return NotFound(new { message = "Order not found" });
        }
        return NoContent();
    }
}
