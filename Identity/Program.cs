using Identity.Data;
using Identity.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Prometheus;
using System.Security.Cryptography;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// Swagger configuration
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "Identity API", Version = "v1" });
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = "JWT Authorization header using the Bearer scheme",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.ApiKey,
        Scheme = "Bearer"
    });
    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

// Database configuration
builder.Services.AddDbContext<IdentityDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("IdentityDb")));

// JWT configuration
var rsaPublicKey = RSA.Create();
var publicKeyPem = File.ReadAllText(builder.Configuration["Rsa:PublicKeyPath"] ?? "certs/public.pem");
rsaPublicKey.ImportFromPem(publicKeyPem);

var rsaPrivateKey = RSA.Create();
var privateKeyPem = File.ReadAllText(builder.Configuration["Rsa:PrivateKeyPath"] ?? "certs/private.pem");
rsaPrivateKey.ImportFromPem(privateKeyPem);

builder.Services.AddSingleton(rsaPublicKey);
builder.Services.AddSingleton(rsaPrivateKey);

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidAudience = builder.Configuration["Jwt:Audience"],
            IssuerSigningKey = new RsaSecurityKey(rsaPublicKey)
        };
    });

builder.Services.AddAuthorization();

// Register services
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IRefreshTokenService, RefreshTokenService>();
builder.Services.AddScoped<IJwtService, JwtService>();

// OpenTelemetry with Zipkin
builder.Services.AddOpenTelemetry()
    .WithTracing(tracerProviderBuilder =>
    {
        tracerProviderBuilder
            .SetResourceBuilder(ResourceBuilder.CreateDefault().AddService("Identity"))
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

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// Health check endpoint
app.MapGet("/actuator/health", () => Results.Ok(new { status = "UP" }));

app.Run();
