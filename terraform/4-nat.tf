# elastic ip for nat gateway
resource "aws_eip" "eshoponsteroids-nat" {
  
}

# nat gateway allows our private subnets to connect to the internet
resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.eshoponsteroids-nat.id
  subnet_id     = aws_subnet.public-us-east-1a.id # place nat gateway in public subnet (because it needs to be accessible from the internet)

  depends_on = [aws_internet_gateway.eshoponsteroids-igw]
}
