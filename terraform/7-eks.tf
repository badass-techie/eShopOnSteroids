# EKS cluster
resource "aws_eks_cluster" "eshoponsteroids" {
  name     = "eshoponsteroids"
  role_arn = aws_iam_role.eshoponsteroids-cluster-role.arn

  vpc_config {
    subnet_ids = [ # the 4 subnets defined earlier
      aws_subnet.private-us-east-1a.id,
      aws_subnet.private-us-east-1b.id,
      aws_subnet.public-us-east-1a.id,
      aws_subnet.public-us-east-1b.id
    ]
  }

  depends_on = [aws_iam_role_policy_attachment.AmazonEKSClusterPolicy] # wait for the IAM role to be created
}