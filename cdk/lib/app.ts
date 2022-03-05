import { App } from "aws-cdk-lib";
import { Runtime } from "aws-cdk-lib/aws-lambda";
import { LambdaStack } from "./stacks/lambda_stack";
import { StorageStack, TABLE_PROPS } from "./stacks/storage_stack";

const app = new App();

const AWS_ENV_CONFIG = {
};
  
const storageStack = new StorageStack(app, "StorageStack", {
    env: AWS_ENV_CONFIG
});

new LambdaStack(app, "LambdaStack", {
    env: AWS_ENV_CONFIG,
    path: "../../../lambda/build/distributions/DailySudokuService-0.1.0.zip",
    handler: "com.ghoulean.dailysudokuservice.lambda.Lambda",
    runtime: Runtime.JAVA_11,
    table: storageStack.table,
});

app.synth();
