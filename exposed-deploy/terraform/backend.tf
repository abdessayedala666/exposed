terraform {
  backend "s3" {
    bucket = "terraform-state-901099688939-eu-west-3-an"
    key    = "terraform.tfstate"
    region = "eu-west-3"
  }
}