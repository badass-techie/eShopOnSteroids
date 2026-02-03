using Microsoft.EntityFrameworkCore;
using Order.Models;

namespace Order.Data;

public class OrderDbContext : DbContext
{
    public OrderDbContext(DbContextOptions<OrderDbContext> options) : base(options)
    {
    }

    public DbSet<Models.Order> Orders { get; set; }
    public DbSet<OrderItem> OrderItems { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Models.Order>(entity =>
        {
            entity.Property(e => e.Status).HasConversion<string>();
            entity.HasMany(o => o.Items)
                .WithOne()
                .OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<OrderItem>(entity =>
        {
            entity.Property(e => e.UnitPrice).HasPrecision(18, 2);
        });
    }
}
