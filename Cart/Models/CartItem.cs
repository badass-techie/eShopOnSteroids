namespace Cart.Models;

public class CartItem
{
    public long ProductId { get; set; }
    public int Quantity { get; set; }
}

public class CartData
{
    public string UserId { get; set; } = string.Empty;
    public List<CartItem> Items { get; set; } = new();
    public DateTime LastModified { get; set; } = DateTime.UtcNow;
}
