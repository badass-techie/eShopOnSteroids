using Microsoft.EntityFrameworkCore;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Order.Data;
using Order.Services;
using Polly;
using Polly.Extensions.Http;
using Prometheus;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo { Title = "Order API", Version = "v1" });
});

// Database configuration
builder.Services.AddDbContext<OrderDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("OrderDb")));

// Register services
builder.Services.AddScoped<IOrderService, OrderService.Services.OrderService>();
builder.Services.AddScoped<IRabbitMQService, RabbitMQService>();
builder.Services.AddSingleton<IProductGrpcClient, ProductGrpcClient>();

// gRPC client with circuit breaker
builder.Services.AddGrpcClient<Order.Grpc.ProductService.ProductServiceClient>(options =>
{
    options.Address = new Uri(builder.Configuration["Grpc:ProductServiceUrl"] ?? "http://localhost:8083");
})
.AddPolicyHandler(GetCircuitBreakerPolicy());

// OpenTelemetry with Zipkin
builder.Services.AddOpenTelemetry()
    .WithTracing(tracerProviderBuilder =>
    {
        tracerProviderBuilder
            .SetResourceBuilder(ResourceBuilder.CreateDefault().AddService("Order"))
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddGrpcClientInstrumentation()
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

static IAsyncPolicy<HttpResponseMessage> GetCircuitBreakerPolicy()
{
    return HttpPolicyExtensions
        .HandleTransientHttpError()
        .CircuitBreakerAsync(5, TimeSpan.FromSeconds(30));
}
