import os
import stripe

stripe.api_key = os.getenv('STRIPE_SECRET_KEY')


def process_stripe_payment(payment_data):
    try:
        intent = stripe.PaymentIntent.create(
            amount=payment_data['amount'] * 100,  # Stripe expects amount in cents
            currency=payment_data['currency'],
            payment_method=payment_data['payerDetails']['cardToken'],  # Use the card token here
            confirm=True,  # Automatically confirm the payment
            description=f'Payment for order number {payment_data["orderNumber"]}',
            automatic_payment_methods={
                'enabled': True,
                'allow_redirects': 'never'
            }
        )

        if intent['status'] == 'succeeded':
            payment_data['resultStatus'] = 'SUCCESS'
            payment_data['resultMessage'] = 'Payment succeeded'
        else:
            payment_data['resultStatus'] = 'FAILED'
            payment_data['resultMessage'] = 'Payment failed - unknown error'
            
            if intent['status'] == 'requires_payment_method':
                payment_data['resultMessage'] = 'Payment failed - invalid card'
            elif intent['status'] == 'requires_action':
                payment_data['resultMessage'] = 'Payment failed - authentication required'
            elif intent['status'] == 'processing':
                payment_data['resultMessage'] = 'Payment failed - processing'
            elif intent['status'] == 'canceled':
                payment_data['resultMessage'] = 'Payment failed - user canceled'
        
        return payment_data
    
    except Exception as e:
        payment_data['resultStatus'] = 'FAILED'
        payment_data['resultMessage'] = f'Payment failed - {e}'
        return payment_data
