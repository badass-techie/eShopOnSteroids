using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Order.Models;

public class OrderItem
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    public string ProductId { get; set; } = string.Empty;

    [Required]
    public string ProductName { get; set; } = string.Empty;

    [Required]
    [Range(0.01, double.MaxValue)]
    public decimal UnitPrice { get; set; }

    [Required]
    [Range(1, int.MaxValue)]
    public int Quantity { get; set; }
}
