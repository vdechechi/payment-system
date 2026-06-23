import json
import boto3
import os

sqs = boto3.client(
    'sqs',
    endpoint_url=os.getenv('AWS_ENDPOINT', 'http://localhost:4566'),
    region_name=os.getenv('AWS_REGION', 'us-east-1'),
    aws_access_key_id='test',
    aws_secret_access_key='test'
)

QUEUE_URL = os.getenv('QUEUE_URL', 'http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/fraud-check-queue')
FRAUD_THRESHOLD = float(os.getenv('FRAUD_THRESHOLD', '5000'))

def check_fraud(payment: dict) -> dict:
    payment_id = payment.get('paymentId')
    amount = float(payment.get('amount', 0))
    payment_type = payment.get('type')

    is_fraud = False
    reason = None

    if amount > FRAUD_THRESHOLD:
        is_fraud = True
        reason = f"Amount {amount} exceeds fraud threshold {FRAUD_THRESHOLD}"

    if payment_type == 'CREDIT_CARD' and amount > 1000:
        is_fraud = True
        reason = f"Credit card payment of {amount} flagged for manual review"

    return {
        'paymentId': payment_id,
        'isFraud': is_fraud,
        'reason': reason,
        'amount': amount,
        'type': payment_type
    }

def lambda_handler(event, context):
    print(f"Received {len(event.get('Records', []))} records from SQS")

    results = []
    for record in event.get('Records', []):
        body = json.loads(record['body'])
        print(f"Processing payment: {body.get('paymentId')}")

        result = check_fraud(body)

        if result['isFraud']:
            print(f"FRAUD DETECTED: {result['reason']}")
        else:
            print(f"Payment {result['paymentId']} approved - no fraud detected")

        results.append(result)

    return {
        'statusCode': 200,
        'body': json.dumps(results)
    }

def poll_queue():
    print("Starting fraud check worker...")
    print(f"Fraud threshold: R$ {FRAUD_THRESHOLD}")

    while True:
        response = sqs.receive_message(
            QueueUrl=QUEUE_URL,
            MaxNumberOfMessages=10,
            WaitTimeSeconds=5
        )

        messages = response.get('Messages', [])
        if not messages:
            print("No messages in queue, waiting...")
            continue

        for message in messages:
            body = json.loads(message['Body'])
            print(f"\nProcessing payment: {body.get('paymentId')}")

            fake_event = {'Records': [{'body': message['Body']}]}
            result = lambda_handler(fake_event, None)

            sqs.delete_message(
                QueueUrl=QUEUE_URL,
                ReceiptHandle=message['ReceiptHandle']
            )
            print(f"Message deleted from queue: {body.get('paymentId')}")

if __name__ == '__main__':
    poll_queue()