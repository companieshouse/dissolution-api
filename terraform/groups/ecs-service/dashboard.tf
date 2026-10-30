resource "aws_cloudwatch_dashboard" "overview" {
  count = var.cloudwatch_dashboard_enabled ? 1 : 0

  dashboard_name = local.dashboard_name

  dashboard_body = templatefile("${path.module}/templates/dashboard.json.tftpl", {
    region                              = var.aws_region
    dissolution_api_log_group_arn       = local.dissolution_api_log_group_arn
    dissolution_web_log_group_arn       = local.dissolution_web_log_group_arn
    transactions_api_log_group_arn      = local.transactions_api_log_group_arn
    chips_filing_consumer_log_group_arn = local.chips_filing_consumer_log_group_arn
    ds01_filing_status_query            = jsonencode(local.filing_status_queries["ds01"])
    llds01_filing_status_query          = jsonencode(local.filing_status_queries["llds01"])
  })
}
