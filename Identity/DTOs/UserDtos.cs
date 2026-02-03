using Identity.Models;
using System.ComponentModel.DataAnnotations;

namespace Identity.DTOs;

public record UserRequest(
    string? Name,
    [Required] [EmailAddress] string Email,
    [Required] string Password,
    string? Bio,
    byte[]? Image
);

public record UserResponse(
    long Id,
    string? Name,
    string Email,
    string? Bio,
    byte[]? Image,
    UserRole Role,
    DateTime Created,
    bool Active
);
