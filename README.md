# DailySudokuService

Cloud service that uploads and saves a new Sudoku puzzle to an AWS DynamoDb table every day.

## Requirements

 - Java 11
 - Node v14.x
 - Typescript v4.x
 - AWS CLI 2

## Installation

Begin by:

1. Adding github personal access tokens with the `read:packages` permission to `~/.gradle/gradle.properties`
2. Configuring an AWS account and region for the AWS CLI to a specific profile, and

Then:

```bash
cd lambda
./gradlew build
cd ../cdk
cdk synth
cdk deploy LambdaStack --profile <your profile>
```

## Learnings

The purpose of this project is to explore basic AWS features and integration with Gradle projects.

 - I personally prefer using CDKs over raw templates; in other words AWS CDK > CloudFormation templates and Terraform CDK > Terraform
 - At the time of this writing, Terraform CDK is *very* young. Documentation is poor and syntax is expected to change
 - Terraform CDK constructs closely mimic AWS CDK constructs. However, occasionally the interfaces are slightly different (e.g. Terraform's `LambdaFunction` accepts a string for the `runtime` field, while AWS's `Function` accepts a `Runtime` enum)
 - Terraform AWS Adapter can convert an AWS CDK construct to a Terraform CDK construct, but currently only supports [EC2 VPC, IAM, and Event Rules](https://github.com/hashicorp/cdktf-aws-cdk/tree/41c3ff3cfc38be087730e41a74ed0773fe85c60f/src/mapping/aws)
 - As much as I wanted to use Terraform CDK over AWS CDK, I can't
 - Setting up separate deployment stacks for DynamoDb and Lambda seemed logical but caused deployment issues during development due to interstack dependencies
