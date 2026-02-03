using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Identity.Models;

[Table("user_table")]
public class User
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    public string? Name { get; set; }

    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;

    public string? Bio { get; set; }

    [MaxLength(1000000)]
    public byte[]? Image { get; set; }

    [Required]
    public UserRole Role { get; set; } = UserRole.CUSTOMER;

    public DateTime Created { get; set; } = DateTime.UtcNow;

    public bool Active { get; set; } = true;
}

public enum UserRole
{
    CUSTOMER,
    ADMIN
}
