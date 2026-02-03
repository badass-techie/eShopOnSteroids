using Identity.DTOs;
using Identity.Services;
using Microsoft.AspNetCore.Mvc;

namespace Identity.Controllers;

[ApiController]
[Route("api/v1/identity/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;
    private readonly IRefreshTokenService _refreshTokenService;

    public AuthController(IAuthService authService, IRefreshTokenService refreshTokenService)
    {
        _authService = authService;
        _refreshTokenService = refreshTokenService;
    }

    /// <summary>
    /// Request an access token
    /// </summary>
    [HttpPost("auth_token")]
    public async Task<ActionResult<AuthResponse>> RequestAccessToken([FromBody] AuthRequest request)
    {
        try
        {
            var response = await _authService.RequestAccessTokenAsync(request);
            return Ok(response);
        }
        catch (UnauthorizedAccessException ex)
        {
            return Unauthorized(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Refresh an access token
    /// </summary>
    [HttpPost("refresh_token")]
    public async Task<ActionResult<AuthResponse>> RefreshToken([FromBody] RefreshTokenRequest request)
    {
        try
        {
            var response = await _authService.RefreshAccessTokenAsync(request);
            return Ok(response);
        }
        catch (UnauthorizedAccessException ex)
        {
            return Unauthorized(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Clear all your refresh tokens
    /// </summary>
    [HttpGet("logout/{email}")]
    public async Task<ActionResult<string>> Logout(string email)
    {
        await _refreshTokenService.DeleteRefreshTokensByUserAsync(email);
        return Accepted("You will be signed out shortly");
    }
}
