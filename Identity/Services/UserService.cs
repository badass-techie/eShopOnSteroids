using Identity.Data;
using Identity.DTOs;
using Identity.Models;
using Microsoft.EntityFrameworkCore;

namespace Identity.Services;

public interface IUserService
{
    Task<UserResponse> CreateUserAsync(UserRequest request);
    Task<UserResponse?> GetUserByEmailAsync(string email);
    Task<UserResponse?> GetUserByIdAsync(long id);
    Task<UserResponse?> UpdateUserAsync(long id, UserRequest request);
    Task<bool> DeleteUserAsync(long id);
    Task<User?> GetUserEntityByEmailAsync(string email);
}

public class UserService : IUserService
{
    private readonly IdentityDbContext _context;

    public UserService(IdentityDbContext context)
    {
        _context = context;
    }

    public async Task<UserResponse> CreateUserAsync(UserRequest request)
    {
        var existingUser = await _context.Users.FirstOrDefaultAsync(u => u.Email == request.Email);
        if (existingUser != null)
        {
            throw new InvalidOperationException("User with this email already exists");
        }

        var user = new User
        {
            Name = request.Name,
            Email = request.Email,
            Password = BCrypt.Net.BCrypt.HashPassword(request.Password),
            Bio = request.Bio,
            Image = request.Image,
            Role = UserRole.CUSTOMER,
            Created = DateTime.UtcNow,
            Active = true
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        return MapToResponse(user);
    }

    public async Task<UserResponse?> GetUserByEmailAsync(string email)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
        return user != null ? MapToResponse(user) : null;
    }

    public async Task<UserResponse?> GetUserByIdAsync(long id)
    {
        var user = await _context.Users.FindAsync(id);
        return user != null ? MapToResponse(user) : null;
    }

    public async Task<UserResponse?> UpdateUserAsync(long id, UserRequest request)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null) return null;

        user.Name = request.Name ?? user.Name;
        user.Bio = request.Bio ?? user.Bio;
        user.Image = request.Image ?? user.Image;

        if (!string.IsNullOrEmpty(request.Password))
        {
            user.Password = BCrypt.Net.BCrypt.HashPassword(request.Password);
        }

        await _context.SaveChangesAsync();
        return MapToResponse(user);
    }

    public async Task<bool> DeleteUserAsync(long id)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null) return false;

        _context.Users.Remove(user);
        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<User?> GetUserEntityByEmailAsync(string email)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
    }

    private static UserResponse MapToResponse(User user)
    {
        return new UserResponse(
            user.Id,
            user.Name,
            user.Email,
            user.Bio,
            user.Image,
            user.Role,
            user.Created,
            user.Active
        );
    }
}
