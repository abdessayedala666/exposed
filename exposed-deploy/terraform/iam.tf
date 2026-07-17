# Create S3 bucket for Ansible SSM transfers
resource "aws_s3_bucket" "ansible_ssm" {
  bucket = "terraform-ansible-ssm"

  force_destroy = true

  tags = {
    Name = "terraform-ansible-ssm"
  }
}


# Create IAM role for EC2
resource "aws_iam_role" "ssm_role" {

  name = "terraform-ssm-role"

  assume_role_policy = jsonencode({

    Version = "2012-10-17"

    Statement = [

      {
        Effect = "Allow"

        Principal = {
          Service = "ec2.amazonaws.com"
        }

        Action = "sts:AssumeRole"
      }

    ]

  })
}


# Attach SSM permissions to the role
resource "aws_iam_role_policy_attachment" "ssm_policy" {

  role = aws_iam_role.ssm_role.name

  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"

}


# Create IAM policy allowing SSM Ansible transfers through S3
resource "aws_iam_policy" "ansible_ssm_s3" {

  name = "ansible-ssm-s3-access"

  policy = jsonencode({

    Version = "2012-10-17"

    Statement = [

      {
        Effect = "Allow"

        Action = [
          "s3:ListBucket"
        ]

        Resource = aws_s3_bucket.ansible_ssm.arn
      },

      {
        Effect = "Allow"

        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ]

        Resource = "${aws_s3_bucket.ansible_ssm.arn}/*"
      }

    ]

  })

}


# Attach S3 permissions to the EC2 role
resource "aws_iam_role_policy_attachment" "ansible_ssm_s3_attachment" {

  role = aws_iam_role.ssm_role.name

  policy_arn = aws_iam_policy.ansible_ssm_s3.arn

}


# Create instance profile for EC2
resource "aws_iam_instance_profile" "ssm_profile" {

  name = "terraform-ssm-profile"

  role = aws_iam_role.ssm_role.name

}