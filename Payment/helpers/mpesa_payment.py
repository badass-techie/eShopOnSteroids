import base64
import json
import os
from . import rabbitmq
import requests
from requests.auth import HTTPBasicAuth
import time


# RabbitMQ queues
queues = {
    'polling-tx-result': 'polling-mpesa-transaction-result',
    'payment-result': 'order-payment-processed'
}

polling_interval = 8000 # milliseconds

# get Oauth token from M-pesa
def get_mpesa_token():
    consumer_key = os.getenv('MPESA_CONSUMER_KEY')
    consumer_secret = os.getenv('MPESA_CONSUMER_SECRET')
    api_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"

    # make a get request using python requests liblary
    r = requests.get(api_URL, auth=HTTPBasicAuth(consumer_key, consumer_secret))

    # return access_token from response
    return r.json()['access_token']


# initiate a 'lipa na mpesa' USSD prompt to the user
def lipa_na_mpesa(payment_data):
    access_token = get_mpesa_token()
    api_url = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"
    timestamp = time.strftime("%Y%m%d%H%M%S")
    password = base64.b64encode((os.getenv('MPESA_BUSINESS_SHORTCODE') + os.getenv('MPESA_PASSKEY') + timestamp).encode()).decode()

    headers = { "Authorization": f"Bearer {access_token}" }
    request = {
        "BusinessShortCode": os.getenv('MPESA_BUSINESS_SHORTCODE'),
        "Password":  password,
        "Timestamp": timestamp,
        "TransactionType": "CustomerPayBillOnline",
        "Amount": payment_data['amount'],
        "PartyA": payment_data['payerDetails']['phoneNumber'],
        "PartyB": os.getenv('MPESA_BUSINESS_SHORTCODE'),
        "PhoneNumber": payment_data['payerDetails']['phoneNumber'],
        "CallBackURL": "https://example.com",
        "AccountReference": "eShopOnSteroids",
        "TransactionDesc": f'Payment for order number {payment_data["orderNumber"]}'
    }
    response = requests.post(api_url, json = request, headers=headers)
    response = response.json()

    if 'CheckoutRequestID' in response:   # success
        # normally we'd provide the API a secure https public endpoint to our application (CallBackURL) to report the transaction result
        # but since we don't have one we'll poll the API every few seconds for the transaction result
        # we won't have to make too many requests because the limit for a transaction to complete is 60 seconds

        # produce delayed message to poll for the transaction result
        payment_data['CheckoutRequestID'] = response['CheckoutRequestID']
        payment_data['resultStatus'] = 'PENDING'
        payment_data['resultMessage'] = 'Payment pending'
        rabbitmq.produce_message(queues['polling-tx-result'], payment_data, polling_interval)
    else:
        payment_data['resultStatus'] = 'FAILED'
        payment_data['resultMessage'] = f"Payment failed - {response['errorMessage']}"
        rabbitmq.produce_message(queues['payment-result'], payment_data)
        print(f"Payment processed for order number {payment_data['orderNumber']}. Result: {payment_data['resultMessage']}")


# Define callback function for polling transaction result
def poll_mpesa_transaction_result(ch, method, properties, body):
    payment_data = json.loads(body.decode('utf-8'))
    access_token = get_mpesa_token()
    api_url = "https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query"

    headers = { "Authorization": f"Bearer {access_token}" }
    timestamp = time.strftime("%Y%m%d%H%M%S")
    password = base64.b64encode((os.getenv('MPESA_BUSINESS_SHORTCODE') + os.getenv('MPESA_PASSKEY') + timestamp).encode()).decode()
    request = {
        "BusinessShortCode": os.getenv('MPESA_BUSINESS_SHORTCODE'),
        "Password": password,
        "Timestamp": timestamp,
        "CheckoutRequestID": payment_data['CheckoutRequestID']
    }
    response = requests.post(api_url, json = request, headers=headers)
    response = response.json()

    if 'ResultCode' in response: # transaction either complete or timed out
        payment_data['resultStatus'] = 'SUCCESS' if response['ResultCode'] == '0' else 'FAILED'
        payment_data['resultMessage'] = ("Payment succeeded - " if response['ResultCode'] == '0' else "Payment failed - ") + response['ResultDesc']
        payment_data.pop('CheckoutRequestID')
        rabbitmq.produce_message(queues['payment-result'], payment_data)
        print(f"Payment processed for order number {payment_data['orderNumber']}. Result: {payment_data['resultMessage']}")

    else: # transaction is still being processed
        rabbitmq.produce_message(queues['polling-tx-result'], payment_data, polling_interval)   # poll again after the interval


# Register callback function to consume messages
rabbitmq.add_consumer(queues['polling-tx-result'], poll_mpesa_transaction_result, delayed=True)
