using Identity.DTOs;
using Identity.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Identity.Controllers;

[ApiController]
[Route("api/v1/identity/user")]
public class UserController : ControllerBase
{
    private readonly IUserService _userService;

    public UserController(IUserService userService)
    {
        _userService = userService;
    }

    /// <summary>
    /// Create a new user
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<UserResponse>> CreateUser([FromBody] UserRequest request)
    {
        try
        {
            var response = await _userService.CreateUserAsync(request);
            return CreatedAtAction(nameof(GetUserById), new { id = response.Id }, response);
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Get user by ID
    /// </summary>
    [HttpGet("{id}")]
    [Authorize]
    public async Task<ActionResult<UserResponse>> GetUserById(long id)
    {
        var user = await _userService.GetUserByIdAsync(id);
        if (user == null)
        {
            return NotFound(new { message = "User not found" });
        }
        return Ok(user);
    }

    /// <summary>
    /// Get user by email
    /// </summary>
    [HttpGet("email/{email}")]
    [Authorize]
    public async Task<ActionResult<UserResponse>> GetUserByEmail(string email)
    {
        var user = await _userService.GetUserByEmailAsync(email);
        if (user == null)
        {
            return NotFound(new { message = "User not found" });
        }
        return Ok(user);
    }

    /// <summary>
    /// Update user
    /// </summary>
    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<UserResponse>> UpdateUser(long id, [FromBody] UserRequest request)
    {
        var user = await _userService.UpdateUserAsync(id, request);
        if (user == null)
        {
            return NotFound(new { message = "User not found" });
        }
        return Ok(user);
    }

    /// <summary>
    /// Delete user
    /// </summary>
    [HttpDelete("{id}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult> DeleteUser(long id)
    {
        var deleted = await _userService.DeleteUserAsync(id);
        if (!deleted)
        {
            return NotFound(new { message = "User not found" });
        }
        return NoContent();
    }
}
