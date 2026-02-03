using Cart.DTOs;
using Cart.Services;
using Microsoft.AspNetCore.Mvc;

namespace Cart.Controllers;

[ApiController]
[Route("api/v1/cart")]
public class CartController : ControllerBase
{
    private readonly ICartService _cartService;

    public CartController(ICartService cartService)
    {
        _cartService = cartService;
    }

    /// <summary>
    /// Get cart for user
    /// </summary>
    [HttpGet("{userId}")]
    public async Task<ActionResult<CartResponse>> GetCart(string userId)
    {
        var cart = await _cartService.GetCartAsync(userId);
        if (cart == null)
        {
            return NotFound(new { message = "Cart not found" });
        }
        return Ok(cart);
    }

    /// <summary>
    /// Add item to cart
    /// </summary>
    [HttpPost("{userId}/items")]
    public async Task<ActionResult<CartResponse>> AddItem(string userId, [FromBody] CartItemRequest item)
    {
        var cart = await _cartService.AddItemAsync(userId, item);
        return Ok(cart);
    }

    /// <summary>
    /// Update item quantity in cart
    /// </summary>
    [HttpPut("{userId}/items/{productId}")]
    public async Task<ActionResult<CartResponse>> UpdateItem(string userId, long productId, [FromBody] int quantity)
    {
        var cart = await _cartService.UpdateItemAsync(userId, productId, quantity);
        if (cart == null)
        {
            return NotFound(new { message = "Item not found in cart" });
        }
        return Ok(cart);
    }

    /// <summary>
    /// Remove item from cart
    /// </summary>
    [HttpDelete("{userId}/items/{productId}")]
    public async Task<ActionResult<CartResponse>> RemoveItem(string userId, long productId)
    {
        var cart = await _cartService.RemoveItemAsync(userId, productId);
        if (cart == null)
        {
            return NotFound(new { message = "Item not found in cart" });
        }
        return Ok(cart);
    }

    /// <summary>
    /// Clear entire cart
    /// </summary>
    [HttpDelete("{userId}")]
    public async Task<ActionResult> ClearCart(string userId)
    {
        var deleted = await _cartService.ClearCartAsync(userId);
        if (!deleted)
        {
            return NotFound(new { message = "Cart not found" });
        }
        return NoContent();
    }
}
