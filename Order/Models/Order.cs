using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Order.Models;

[Table("order_table")]
public class Order
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    public long UserId { get; set; }

    [Required]
    public string OrderNumber { get; set; } = string.Empty;

    public List<OrderItem> Items { get; set; } = new();

    [Required]
    public OrderStatus Status { get; set; } = OrderStatus.PENDING;

    [Required]
    public string DeliveryAddress { get; set; } = string.Empty;

    public DateTime Created { get; set; } = DateTime.UtcNow;
}

public enum OrderStatus
{
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
