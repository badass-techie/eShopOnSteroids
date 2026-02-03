using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Identity.Models;

[Table("refresh_token")]
public class RefreshToken
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    public string Token { get; set; } = string.Empty;

    [Required]
    [ForeignKey("User")]
    public long UserId { get; set; }

    public User? User { get; set; }

    public DateTime ExpiryDate { get; set; }

    public DateTime Created { get; set; } = DateTime.UtcNow;
}
