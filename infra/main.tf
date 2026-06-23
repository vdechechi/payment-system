terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    sqs      = "http://localhost:4566"
    dynamodb = "http://localhost:4566"
    sns      = "http://localhost:4566"
    lambda   = "http://localhost:4566"
    iam      = "http://localhost:4566"
  }
}

resource "aws_sqs_queue" "fraud_check_queue" {
  name                       = "fraud-check-queue"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 86400
  receive_wait_time_seconds  = 5
}

resource "aws_dynamodb_table" "payment_events" {
  name         = "payment-events"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "paymentId"
  range_key    = "createdAt"

  attribute {
    name = "paymentId"
    type = "S"
  }

  attribute {
    name = "createdAt"
    type = "S"
  }
}

resource "aws_iam_role" "lambda_role" {
  name = "fraud-check-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_lambda_function" "fraud_check" {
  filename         = "fraud-check.zip"
  function_name    = "fraud-check"
  role             = aws_iam_role.lambda_role.arn
  handler          = "handler.lambda_handler"
  runtime          = "python3.12"
  source_code_hash = filebase64sha256("fraud-check.zip")

  environment {
    variables = {
      AWS_ENDPOINT   = "http://localhost:4566"
      QUEUE_URL      = "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/fraud-check-queue"
      FRAUD_THRESHOLD = "5000"
    }
  }
}

resource "aws_lambda_event_source_mapping" "sqs_trigger" {
  event_source_arn = aws_sqs_queue.fraud_check_queue.arn
  function_name    = aws_lambda_function.fraud_check.arn
  batch_size       = 10
  enabled          = true
}