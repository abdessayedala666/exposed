resource "aws_instance" "terraform" {


  ami = "ami-0e1c4170d9c01184b"


  instance_type = "t3.small"


  iam_instance_profile = aws_iam_instance_profile.ssm_profile.name

  key_name = aws_key_pair.ansible.key_name

  vpc_security_group_ids = [
    aws_security_group.terraform_sg.id
  ]

  tags = {

    Name = "terraform"

  }

}