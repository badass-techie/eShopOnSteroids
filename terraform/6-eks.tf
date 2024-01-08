# EKS cluster and node group(s)
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  vpc_id = aws_vpc.eshoponsteroids-vpc.id
  subnet_ids = [
    aws_subnet.private-us-east-1a.id,
    aws_subnet.private-us-east-1b.id
  ]

  cluster_name                   = "eshoponsteroids"
  cluster_version                = "1.28"
  cluster_endpoint_public_access = true
  cluster_addons = {
    coredns = {
      most_recent = true
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent = true
    }
    aws-ebs-csi-driver = { # allows us to request EBS volumes for our kubernetes PVCs
      most_recent = true
    }
  }

  eks_managed_node_groups = {
    default = {
      min_size     = 1
      max_size     = 1
      desired_size = 1

      instance_types = ["t3.xlarge"] # ON_DEMAND or SPOT (which can be taken away at any time)
      capacity_type  = "ON_DEMAND"

      # Needed by the aws-ebs-csi-driver
      iam_role_additional_policies = {
        AmazonEBSCSIDriverPolicy = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
      }
    }
  }
}