resource "aws_security_group" "terraform_sg" {
  name = "terraform-ssh"

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] 
  }
}
resource "tls_private_key" "ansible" {
  algorithm = "RSA"
  rsa_bits  = 4096
}
resource "aws_key_pair" "ansible" {
  key_name   = "ansible-key"
  public_key = tls_private_key.ansible.public_key_openssh
}
resource "local_file" "private_key" {
  filename        = "${path.module}/ansible-key.pem"
  content         = tls_private_key.ansible.private_key_pem
  file_permission = "0600"
}