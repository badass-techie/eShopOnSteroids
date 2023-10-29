# allows our VPC to be accessible from the internet
resource "aws_internet_gateway" "eshoponsteroids-igw" {
  vpc_id = aws_vpc.eshoponsteroids-vpc.id
}
