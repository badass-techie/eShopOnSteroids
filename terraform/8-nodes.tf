# node group
resource "aws_eks_node_group" "eshoponsteroids-node-group" {
  cluster_name    = aws_eks_cluster.eshoponsteroids.name
  node_group_name = "eshoponsteroids-node-group"
  node_role_arn   = aws_iam_role.eshoponsteroids-node-group-role.arn

  subnet_ids = [ # we want the nodes to be in the private subnets
    aws_subnet.private-us-east-1a.id,
    aws_subnet.private-us-east-1b.id
  ]

  capacity_type  = "ON_DEMAND" # ON_DEMAND or SPOT (which can be taken away at any time)
  instance_types = ["t3.large"]

  scaling_config {
    desired_size = 1
    max_size     = 1 # we only want 1 node in each AZ
    min_size     = 0
  }

  update_config {
    max_unavailable = 1
  }

  tags = {
    Name = "eshoponsteroids-node-group"
  }

  depends_on = [ # wait for the IAM role to be created
    aws_iam_role_policy_attachment.AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.AmazonEKS_CNI_Policy,
    aws_iam_role_policy_attachment.AmazonEC2ContainerRegistryReadOnly,
  ]
}