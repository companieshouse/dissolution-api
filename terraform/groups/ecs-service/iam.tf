resource "aws_iam_role" "task_role" {
  name               = "${var.environment}-${local.service_name}-task-role"
  path               = "/"
  assume_role_policy = data.aws_iam_policy_document.task_assume.json
}

data "aws_iam_policy_document" "task_assume" {
  statement {
    sid     = "AllowTaskAssumeRole"
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "task_policy" {
  name        = "${var.environment}-${local.service_name}-task-policy"
  policy      = data.aws_iam_policy_document.task_policy.json
}

data "aws_iam_policy_document" "task_policy" {
  statement {
    sid       = "AllowS3ListBuckets"
    effect    = "Allow"
    actions   = [
      "s3:ListBucket"
    ]
    resources = [
      "arn:aws:s3:::${local.dissolutions_bucket_name}"
    ]
  }

  statement {
    sid       = "AllowS3ReadObjects"
    effect    = "Allow"
    actions   = [
      "s3:GetBucketPolicy",
      "s3:GetLifecycleConfiguration",
      "s3:GetObject"
    ]
    resources = [
      "arn:aws:s3:::${local.dissolutions_bucket_name}/*"
    ]
  }
}

resource "aws_iam_role_policy_attachment" "task_role_attachment" {
  role       = aws_iam_role.task_role.name
  policy_arn = aws_iam_policy.task_policy.arn
}
