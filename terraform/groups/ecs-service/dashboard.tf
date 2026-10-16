data "aws_caller_identity" "current" {}

locals {
  dashboard_name = "${local.name_prefix}-overview"

  dissolution_api_log_group_arn = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/ecs/${local.name_prefix}/${local.service_name}"
}

resource "aws_cloudwatch_dashboard" "overview" {
  dashboard_name = local.dashboard_name

  dashboard_body = templatefile("${path.module}/templates/dashboard.json.tftpl", {
    region                        = var.aws_region
    dissolution_api_log_group_arn = local.dissolution_api_log_group_arn
  })
}
