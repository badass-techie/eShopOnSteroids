using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Prometheus;
using System.Security.Cryptography;

var builder = WebApplication.CreateBuilder(args);

// JWT configuration
var rsaPublicKey = RSA.Create();
var publicKeyPem = File.ReadAllText(builder.Configuration["Rsa:PublicKeyPath"] ?? "certs/public.pem");
rsaPublicKey.ImportFromPem(publicKeyPem);

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

// CORS
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// Reverse proxy
builder.Services.AddReverseProxy()
    .LoadFromConfig(builder.Configuration.GetSection("ReverseProxy"));

// OpenTelemetry with Zipkin
builder.Services.AddOpenTelemetry()
    .WithTracing(tracerProviderBuilder =>
    {
        tracerProviderBuilder
            .SetResourceBuilder(ResourceBuilder.CreateDefault().AddService("ApiGateway"))
            .AddAspNetCoreInstrumentation()
            .AddHttpClientInstrumentation()
            .AddZipkinExporter(options =>
            {
                options.Endpoint = new Uri(builder.Configuration["Zipkin:Endpoint"] ?? "http://localhost:9411/api/v2/spans");
            });
    });

var app = builder.Build();

// Prometheus metrics
app.UseMetricServer();
app.UseHttpMetrics();

app.UseCors();

app.UseAuthentication();
app.UseAuthorization();

app.MapReverseProxy();

// Health check endpoint
app.MapGet("/actuator/health", () => Results.Ok(new { status = "UP" }));

app.Run();
