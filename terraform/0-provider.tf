# Specify the provider and region
provider "aws" {
  region = "us-east-1"
}

# specify the required provider and version
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}