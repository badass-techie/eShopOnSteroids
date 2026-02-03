using Microsoft.Extensions.Hosting;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System.Text;
using System.Text.Json;

namespace Product.Services;

public interface IRabbitMQConsumer
{
    Task StartAsync(CancellationToken cancellationToken);
    Task StopAsync(CancellationToken cancellationToken);
}

public class RabbitMQConsumer : BackgroundService, IRabbitMQConsumer
{
    private readonly IProductService _productService;
    private readonly IConfiguration _configuration;
    private readonly ILogger<RabbitMQConsumer> _logger;
    private IConnection? _connection;
    private IModel? _channel;

    public RabbitMQConsumer(
        IServiceScopeFactory serviceScopeFactory,
        IConfiguration configuration,
        ILogger<RabbitMQConsumer> logger)
    {
        var scope = serviceScopeFactory.CreateScope();
        _productService = scope.ServiceProvider.GetRequiredService<IProductService>();
        _configuration = configuration;
        _logger = logger;
    }

    public override Task StartAsync(CancellationToken cancellationToken)
    {
        var factory = new ConnectionFactory
        {
            HostName = _configuration["RabbitMQ:Host"] ?? "localhost",
            Port = int.Parse(_configuration["RabbitMQ:Port"] ?? "5672"),
            UserName = _configuration["RabbitMQ:Username"] ?? "guest",
            Password = _configuration["RabbitMQ:Password"] ?? "guest"
        };

        _connection = factory.CreateConnection();
        _channel = _connection.CreateModel();

        _channel.QueueDeclare(
            queue: "stock_queue",
            durable: true,
            exclusive: false,
            autoDelete: false,
            arguments: null
        );

        return base.StartAsync(cancellationToken);
    }

    protected override Task ExecuteAsync(CancellationToken stoppingToken)
    {
        var consumer = new EventingBasicConsumer(_channel);
        consumer.Received += async (model, ea) =>
        {
            var body = ea.Body.ToArray();
            var message = Encoding.UTF8.GetString(body);

            try
            {
                var stockUpdates = JsonSerializer.Deserialize<List<StockUpdate>>(message);
                if (stockUpdates != null)
                {
                    foreach (var update in stockUpdates)
                    {
                        await _productService.UpdateStockAsync(update.ProductId, update.Quantity);
                    }
                    _logger.LogInformation("Stock updated successfully");
                }

                _channel?.BasicAck(deliveryTag: ea.DeliveryTag, multiple: false);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing stock update message");
                _channel?.BasicNack(deliveryTag: ea.DeliveryTag, multiple: false, requeue: true);
            }
        };

        _channel?.BasicConsume(queue: "stock_queue", autoAck: false, consumer: consumer);

        return Task.CompletedTask;
    }

    public override async Task StopAsync(CancellationToken cancellationToken)
    {
        _channel?.Close();
        _connection?.Close();
        await base.StopAsync(cancellationToken);
    }

    private record StockUpdate(string ProductId, int Quantity);
}
