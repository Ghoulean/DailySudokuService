import path = require("path");

import { Construct } from "constructs";
import { Duration, Stack, StackProps } from "aws-cdk-lib";
import { Rule, Schedule } from "aws-cdk-lib/aws-events";
import { LambdaFunction } from "aws-cdk-lib/aws-events-targets";
import { Code, Function, Runtime } from 'aws-cdk-lib/aws-lambda';
import { Table } from "aws-cdk-lib/aws-dynamodb";
import { getDynamoDbAccessPolicy } from "../helper/iam";

export interface LambdaStackProps extends StackProps {
  path: string,
  handler: string,
  runtime: Runtime,
  table: Table,
}

export class LambdaStack extends Stack {
  public readonly lambda: Function;
  public readonly rule: Rule;

  constructor(scope: Construct, name: string, props: LambdaStackProps) {
    super(scope, name, props);

    this.lambda = new Function(this, "dailySudokuServiceLambda", {
      code: Code.fromAsset(path.resolve(__dirname, props.path)),
      environment: {
        "TABLE_NAME": props.table.tableName,
        "RETRIES": "0",
      },
      handler: props.handler,
      memorySize: 512,
      runtime: props.runtime,
      timeout: Duration.minutes(15),
    });

    this.lambda.addToRolePolicy(getDynamoDbAccessPolicy(props.table.tableArn));

    this.rule = new Rule(this, "dailyScheduler", {
      description: "Run the DailySudokuService Lambda once a day",
      enabled: true,
      ruleName: "DailySudokuService_DailyScheduler",
      schedule: Schedule.cron({
        hour: '4',
        minute: '0',
      }),
      targets: [new LambdaFunction(this.lambda, {})]
    });
  }
}
