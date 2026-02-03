using Cart.DTOs;
using Cart.Models;
using StackExchange.Redis;
using System.Text.Json;

namespace Cart.Services;

public interface ICartService
{
    Task<CartResponse?> GetCartAsync(string userId);
    Task<CartResponse> AddItemAsync(string userId, CartItemRequest item);
    Task<CartResponse?> UpdateItemAsync(string userId, long productId, int quantity);
    Task<CartResponse?> RemoveItemAsync(string userId, long productId);
    Task<bool> ClearCartAsync(string userId);
}

public class CartService : ICartService
{
    private readonly IDatabase _redis;
    private const string CartKeyPrefix = "cart:";

    public CartService(IConnectionMultiplexer redis)
    {
        _redis = redis.GetDatabase();
    }

    public async Task<CartResponse?> GetCartAsync(string userId)
    {
        var cartJson = await _redis.StringGetAsync(GetCartKey(userId));
        if (cartJson.IsNullOrEmpty)
            return null;

        var cart = JsonSerializer.Deserialize<CartData>(cartJson!);
        return cart != null ? MapToResponse(cart) : null;
    }

    public async Task<CartResponse> AddItemAsync(string userId, CartItemRequest item)
    {
        var cart = await GetOrCreateCartAsync(userId);

        var existingItem = cart.Items.FirstOrDefault(i => i.ProductId == item.ProductId);
        if (existingItem != null)
        {
            existingItem.Quantity += item.Quantity;
        }
        else
        {
            cart.Items.Add(new CartItem
            {
                ProductId = item.ProductId,
                Quantity = item.Quantity
            });
        }

        cart.LastModified = DateTime.UtcNow;
        await SaveCartAsync(userId, cart);

        return MapToResponse(cart);
    }

    public async Task<CartResponse?> UpdateItemAsync(string userId, long productId, int quantity)
    {
        var cart = await GetOrCreateCartAsync(userId);

        var item = cart.Items.FirstOrDefault(i => i.ProductId == productId);
        if (item == null)
            return null;

        item.Quantity = quantity;
        cart.LastModified = DateTime.UtcNow;
        await SaveCartAsync(userId, cart);

        return MapToResponse(cart);
    }

    public async Task<CartResponse?> RemoveItemAsync(string userId, long productId)
    {
        var cart = await GetOrCreateCartAsync(userId);

        var item = cart.Items.FirstOrDefault(i => i.ProductId == productId);
        if (item == null)
            return null;

        cart.Items.Remove(item);
        cart.LastModified = DateTime.UtcNow;
        await SaveCartAsync(userId, cart);

        return MapToResponse(cart);
    }

    public async Task<bool> ClearCartAsync(string userId)
    {
        return await _redis.KeyDeleteAsync(GetCartKey(userId));
    }

    private async Task<CartData> GetOrCreateCartAsync(string userId)
    {
        var cartJson = await _redis.StringGetAsync(GetCartKey(userId));
        if (!cartJson.IsNullOrEmpty)
        {
            return JsonSerializer.Deserialize<CartData>(cartJson!) ?? new CartData { UserId = userId };
        }

        return new CartData { UserId = userId };
    }

    private async Task SaveCartAsync(string userId, CartData cart)
    {
        var cartJson = JsonSerializer.Serialize(cart);
        await _redis.StringSetAsync(GetCartKey(userId), cartJson, TimeSpan.FromDays(30));
    }

    private static string GetCartKey(string userId) => $"{CartKeyPrefix}{userId}";

    private static CartResponse MapToResponse(CartData cart)
    {
        return new CartResponse(
            cart.UserId,
            cart.Items.Select(i => new CartItemResponse(i.ProductId, i.Quantity)).ToList(),
            cart.LastModified
        );
    }
}
