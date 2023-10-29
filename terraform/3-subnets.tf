# EKS requires 2 public and private subnets in different availability zones

resource "aws_subnet" "public-us-east-1a" {
  vpc_id                  = aws_vpc.eshoponsteroids-vpc.id
  cidr_block              = "10.0.64.0/19"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = true

  tags = {
    "Name"                                  = "public-us-east-1a"
    "kubernetes.io/role/elb"                = "1"      # public subnets are used for external ELBs
    "kubernetes.io/cluster/eshoponsteroids" = "shared" # tag this subnet as owned by the cluster 'eshoponsteroids'
  }
}

resource "aws_subnet" "public-us-east-1b" {
  vpc_id                  = aws_vpc.eshoponsteroids-vpc.id
  cidr_block              = "10.0.96.0/19"
  availability_zone       = "us-east-1b"
  map_public_ip_on_launch = true

  tags = {
    "Name"                                  = "public-us-east-1b"
    "kubernetes.io/role/elb"                = "1"
    "kubernetes.io/cluster/eshoponsteroids" = "shared"
  }
}

resource "aws_subnet" "private-us-east-1a" {
  vpc_id            = aws_vpc.eshoponsteroids-vpc.id
  cidr_block        = "10.0.0.0/19"
  availability_zone = "us-east-1a"

  tags = {
    "Name"                                  = "private-us-east-1a"
    "kubernetes.io/role/internal-elb"       = "1"      # private subnets are used for internal ELBs
    "kubernetes.io/cluster/eshoponsteroids" = "shared" # owned if only used by this cluster or shared if used by multiple clusters or external services
  }
}

resource "aws_subnet" "private-us-east-1b" {
  vpc_id            = aws_vpc.eshoponsteroids-vpc.id
  cidr_block        = "10.0.32.0/19"
  availability_zone = "us-east-1b"

  tags = {
    "Name"                                  = "private-us-east-1b"
    "kubernetes.io/role/internal-elb"       = "1"
    "kubernetes.io/cluster/eshoponsteroids" = "shared"
  }
}
