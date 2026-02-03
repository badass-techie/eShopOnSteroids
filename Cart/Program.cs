using Cart.Services;
using Microsoft.OpenApi.Models;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Prometheus;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "Cart API", Version = "v1" });
});

// Redis configuration
var redisConnection = ConnectionMultiplexer.Connect(
    builder.Configuration.GetConnectionString("Redis") ?? "localhost:6379"
);
builder.Services.AddSingleton<IConnectionMultiplexer>(redisConnection);

// Register services
builder.Services.AddScoped<ICartService, CartService>();

// OpenTelemetry with Zipkin
builder.Services.AddOpenTelemetry()
    .WithTracing(tracerProviderBuilder =>
    {
        tracerProviderBuilder
            .SetResourceBuilder(ResourceBuilder.CreateDefault().AddService("Cart"))
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddRedisInstrumentation(redisConnection)
            .AddZipkinExporter(options =>
            {
                options.Endpoint = new Uri(builder.Configuration["Zipkin:Endpoint"] ?? "http://localhost:9411/api/v2/spans");
            });
    });

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Prometheus metrics
app.UseMetricServer();
app.UseHttpMetrics();

app.UseAuthorization();

app.MapControllers();

// Health check endpoint
app.MapGet("/actuator/health", () => Results.Ok(new { status = "UP" }));

app.Run();
