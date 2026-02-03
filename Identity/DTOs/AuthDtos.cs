using System.ComponentModel.DataAnnotations;

namespace Identity.DTOs;

public record AuthRequest(
    [Required] [EmailAddress] string Email,
    [Required] string Password
);

public record AuthResponse(
    string AccessToken,
    string RefreshToken
);

public record RefreshTokenRequest(
    [Required] string RefreshToken
);
