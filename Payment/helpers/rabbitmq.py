import json
import os
import pika

# RabbitMQ connection parameters
rabbitmq_url = os.getenv('RABBITMQ_BASE')
exchange_name = 'eshoponsteroids'
delayed_exchange_name = 'delayed-messages'

# Establish connection to RabbitMQ
connection = pika.BlockingConnection(pika.URLParameters(rabbitmq_url))
channel = connection.channel()

# Declare the exchanges
channel.exchange_declare(exchange=exchange_name, exchange_type='topic', durable=True)
channel.exchange_declare(exchange=delayed_exchange_name, exchange_type='x-delayed-message', durable=True, arguments={'x-delayed-type': 'direct'})

# Produce a message
def produce_message(queueName, message, delay=0):
    message = json.dumps(message)
    
    if delay > 0:
        channel.queue_declare(queue=queueName, durable=True, arguments={'x-dead-letter-exchange': delayed_exchange_name, 'x-dead-letter-routing-key': queueName})
        channel.queue_bind(exchange=delayed_exchange_name, queue=queueName, routing_key=queueName)
        channel.basic_publish(
            exchange=delayed_exchange_name, 
            routing_key=queueName, 
            body=message,
            properties=pika.BasicProperties(headers={'x-delay': delay}) # delay message by number of seconds specified
        )
    else:
        channel.queue_declare(queue=queueName, durable=True)
        channel.queue_bind(exchange=exchange_name, queue=queueName, routing_key=queueName)
        channel.basic_publish(
            exchange=exchange_name, 
            routing_key=queueName, 
            body=message
        )

# Register a callback function to consume messages
def add_consumer(queueName, callback, delayed=False):
    if delayed:
        channel.queue_declare(queue=queueName, durable=True, arguments={'x-dead-letter-exchange': delayed_exchange_name, 'x-dead-letter-routing-key': queueName})
        channel.queue_bind(exchange=delayed_exchange_name, queue=queueName, routing_key=queueName)
    else:
        channel.queue_declare(queue=queueName, durable=True)
        channel.queue_bind(exchange=exchange_name, queue=queueName, routing_key=queueName)

    channel.basic_consume(
        queue=queueName, 
        on_message_callback=callback, 
        auto_ack=True
    )

# Listen for messages
def start_consuming():
    channel.start_consuming()
    connection.close()
