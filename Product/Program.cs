using Microsoft.OpenApi.Models;
using MongoDB.Driver;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Product.Services;
using Prometheus;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "Product API", Version = "v1" });
});

// MongoDB configuration
var mongoClient = new MongoClient(builder.Configuration.GetConnectionString("ProductDb"));
var mongoDatabase = mongoClient.GetDatabase(builder.Configuration["MongoDB:DatabaseName"] ?? "productdb");

builder.Services.AddSingleton<IMongoClient>(mongoClient);
builder.Services.AddSingleton(mongoDatabase);

// Register services
builder.Services.AddScoped<IProductService, ProductService>();
builder.Services.AddScoped<IBrandService, BrandService>();
builder.Services.AddSingleton<IRabbitMQConsumer, RabbitMQConsumer>();
builder.Services.AddHostedService<RabbitMQConsumer>();

// gRPC
builder.Services.AddGrpc();

// OpenTelemetry with Zipkin
builder.Services.AddOpenTelemetry()
    .WithTracing(tracerProviderBuilder =>
    {
        tracerProviderBuilder
            .SetResourceBuilder(ResourceBuilder.CreateDefault().AddService("Product"))
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
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
app.MapGrpcService<ProductGrpcService>();

// Health check endpoint
app.MapGet("/actuator/health", () => Results.Ok(new { status = "UP" }));

app.Run();
