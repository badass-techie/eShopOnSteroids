using Identity.DTOs;
using Identity.Models;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;

namespace Identity.Services;

public interface IJwtService
{
    string GenerateAccessToken(User user);
    string GenerateRefreshToken();
}

public class JwtService : IJwtService
{
    private readonly IConfiguration _configuration;
    private readonly RSA _rsaPrivateKey;

    public JwtService(IConfiguration configuration, RSA rsaPrivateKey)
    {
        _configuration = configuration;
        _rsaPrivateKey = rsaPrivateKey;
    }

    public string GenerateAccessToken(User user)
    {
        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, user.Email),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new Claim(ClaimTypes.Role, user.Role.ToString()),
            new Claim("userId", user.Id.ToString())
        };

        var credentials = new SigningCredentials(
            new RsaSecurityKey(_rsaPrivateKey),
            SecurityAlgorithms.RsaSha256
        );

        var token = new JwtSecurityToken(
            issuer: _configuration["Jwt:Issuer"],
            audience: _configuration["Jwt:Audience"],
            claims: claims,
            expires: DateTime.UtcNow.AddSeconds(
                int.Parse(_configuration["Jwt:AccessTokenExpiration"] ?? "3600")
            ),
            signingCredentials: credentials
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public string GenerateRefreshToken()
    {
        var randomNumber = new byte[32];
        using var rng = RandomNumberGenerator.Create();
        rng.GetBytes(randomNumber);
        return Convert.ToBase64String(randomNumber);
    }
}
