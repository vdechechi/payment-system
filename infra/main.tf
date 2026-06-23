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