import json
from helpers import rabbitmq
from helpers import stripe_payment # Stripe payment processing module
from helpers import mpesa_payment # M-Pesa payment processing module

with open('banner.txt', 'r') as banner:
    print(banner.read())

# RabbitMQ queues
queues = {
    'awaiting-payment': 'order-awaiting-payment',
    'payment-result': 'order-payment-processed'
}

# Define callback function for processing payment
def callback(ch, method, properties, body):
    payment_data = json.loads(body.decode('utf-8'))
    print(f"Received payment request for order number {payment_data['orderNumber']}")

    # Handle payment using specified payment method
    if payment_data['paymentMethod'] == 'stripe':
        payment_result = stripe_payment.process_stripe_payment(payment_data)

        # Produce message indicating payment success or failure
        rabbitmq.produce_message(queues['payment-result'], payment_result)
        print(f"Payment processed for order number {payment_data['orderNumber']}. Result: {payment_result['resultMessage']}")

    elif payment_data['paymentMethod'] == 'mpesa':
        mpesa_payment.lipa_na_mpesa(payment_data)   # this will be handled asynchronously

    else:
        print(f"Payment method {payment_data['paymentMethod']} not supported")

# Register callback function to consume messages
rabbitmq.add_consumer(queues['awaiting-payment'], callback)


if __name__ == '__main__':
    print('Waiting for payment requests...')
    rabbitmq.start_consuming()
