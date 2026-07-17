terraform {
  backend "s3" {
    bucket = "ansible-bucket-2026"
    key    = "terraform.tfstate"
    region = "eu-west-3"
  }
}