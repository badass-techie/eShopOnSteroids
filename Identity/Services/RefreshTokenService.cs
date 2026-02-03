using Identity.Data;
using Identity.Models;
using Microsoft.EntityFrameworkCore;

namespace Identity.Services;

public interface IRefreshTokenService
{
    Task<RefreshToken> CreateRefreshTokenAsync(User user);
    Task<RefreshToken?> GetRefreshTokenAsync(string token);
    Task DeleteRefreshTokensByUserAsync(string email);
    Task DeleteRefreshTokenAsync(string token);
}

public class RefreshTokenService : IRefreshTokenService
{
    private readonly IdentityDbContext _context;
    private readonly IJwtService _jwtService;
    private readonly IConfiguration _configuration;

    public RefreshTokenService(
        IdentityDbContext context,
        IJwtService jwtService,
        IConfiguration configuration)
    {
        _context = context;
        _jwtService = jwtService;
        _configuration = configuration;
    }

    public async Task<RefreshToken> CreateRefreshTokenAsync(User user)
    {
        var refreshToken = new RefreshToken
        {
            Token = _jwtService.GenerateRefreshToken(),
            UserId = user.Id,
            ExpiryDate = DateTime.UtcNow.AddSeconds(
                int.Parse(_configuration["Jwt:RefreshTokenExpiration"] ?? "2592000")
            ),
            Created = DateTime.UtcNow
        };

        _context.RefreshTokens.Add(refreshToken);
        await _context.SaveChangesAsync();

        return refreshToken;
    }

    public async Task<RefreshToken?> GetRefreshTokenAsync(string token)
    {
        return await _context.RefreshTokens
            .Include(rt => rt.User)
            .FirstOrDefaultAsync(rt => rt.Token == token && rt.ExpiryDate > DateTime.UtcNow);
    }

    public async Task DeleteRefreshTokensByUserAsync(string email)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
        if (user != null)
        {
            var tokens = _context.RefreshTokens.Where(rt => rt.UserId == user.Id);
            _context.RefreshTokens.RemoveRange(tokens);
            await _context.SaveChangesAsync();
        }
    }

    public async Task DeleteRefreshTokenAsync(string token)
    {
        var refreshToken = await _context.RefreshTokens.FirstOrDefaultAsync(rt => rt.Token == token);
        if (refreshToken != null)
        {
            _context.RefreshTokens.Remove(refreshToken);
            await _context.SaveChangesAsync();
        }
    }
}
