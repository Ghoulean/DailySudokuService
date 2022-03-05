import { Construct } from "constructs";
import { AttributeType, BillingMode, Table } from "aws-cdk-lib/aws-dynamodb";
import { Stack, StackProps } from "aws-cdk-lib";

export const TABLE_PROPS = {
  partitionKey: {
    name: "puzzledate",
    type: AttributeType.STRING,
  },
  billingMode: BillingMode.PAY_PER_REQUEST,
}

export interface StorageStackProps extends StackProps {
}

export class StorageStack extends Stack {
  public readonly table: Table;

  constructor(scope: Construct, name: string, props: StorageStackProps) {
    super(scope, name, props);

    this.table = new Table(this, "DailySudokuServiceTable", {
      partitionKey: TABLE_PROPS.partitionKey,
      billingMode: TABLE_PROPS.billingMode,
    });
  }
}
