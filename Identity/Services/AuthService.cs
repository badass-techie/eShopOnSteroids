using Identity.DTOs;
using Identity.Models;

namespace Identity.Services;

public interface IAuthService
{
    Task<AuthResponse> RequestAccessTokenAsync(AuthRequest request);
    Task<AuthResponse> RefreshAccessTokenAsync(RefreshTokenRequest request);
}

public class AuthService : IAuthService
{
    private readonly IUserService _userService;
    private readonly IJwtService _jwtService;
    private readonly IRefreshTokenService _refreshTokenService;

    public AuthService(
        IUserService userService,
        IJwtService jwtService,
        IRefreshTokenService refreshTokenService)
    {
        _userService = userService;
        _jwtService = jwtService;
        _refreshTokenService = refreshTokenService;
    }

    public async Task<AuthResponse> RequestAccessTokenAsync(AuthRequest request)
    {
        var user = await _userService.GetUserEntityByEmailAsync(request.Email);
        
        if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.Password))
        {
            throw new UnauthorizedAccessException("Invalid email or password");
        }

        if (!user.Active)
        {
            throw new UnauthorizedAccessException("User account is not active");
        }

        var accessToken = _jwtService.GenerateAccessToken(user);
        var refreshToken = await _refreshTokenService.CreateRefreshTokenAsync(user);

        return new AuthResponse(accessToken, refreshToken.Token);
    }

    public async Task<AuthResponse> RefreshAccessTokenAsync(RefreshTokenRequest request)
    {
        var refreshToken = await _refreshTokenService.GetRefreshTokenAsync(request.RefreshToken);
        
        if (refreshToken == null || refreshToken.User == null)
        {
            throw new UnauthorizedAccessException("Invalid refresh token");
        }

        var accessToken = _jwtService.GenerateAccessToken(refreshToken.User);
        
        // Delete old refresh token and create new one
        await _refreshTokenService.DeleteRefreshTokenAsync(request.RefreshToken);
        var newRefreshToken = await _refreshTokenService.CreateRefreshTokenAsync(refreshToken.User);

        return new AuthResponse(accessToken, newRefreshToken.Token);
    }
}
