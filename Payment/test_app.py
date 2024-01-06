import json
import pytest
from unittest.mock import patch, MagicMock
from app import callback

class TestPaymentProcessing:
    @pytest.fixture(autouse=True)
    def setup(self):
        self.payment_data_stripe = {
            'orderNumber': '1234',
            'paymentMethod': 'stripe',
            'amount': 100.0
        }

        self.payment_data_mpesa = {
            'orderNumber': '1234',
            'paymentMethod': 'mpesa',
            'amount': 100.0
        }

        self.payment_data_invalid = {
            'orderNumber': '1234',
            'paymentMethod': 'invalid',
            'amount': 100.0
        }

    @patch('app.stripe_payment.process_stripe_payment')
    @patch('app.rabbitmq.produce_message')
    def test_callback_stripe(self, mock_produce_message, mock_process_stripe_payment):
        mock_process_stripe_payment.return_value = {'resultMessage': 'Payment successful'}
        callback(None, None, None, json.dumps(self.payment_data_stripe).encode('utf-8'))
        mock_process_stripe_payment.assert_called_once_with(self.payment_data_stripe)
        mock_produce_message.assert_called_once()

    @patch('app.mpesa_payment.lipa_na_mpesa')
    def test_callback_mpesa(self, mock_lipa_na_mpesa):
        callback(None, None, None, json.dumps(self.payment_data_mpesa).encode('utf-8'))
        mock_lipa_na_mpesa.assert_called_once_with(self.payment_data_mpesa)

    def test_callback_invalid(self, capsys):
        callback(None, None, None, json.dumps(self.payment_data_invalid).encode('utf-8'))
        captured = capsys.readouterr()
        assert 'Payment method invalid not supported' in captured.out

