package com.ghoulean.dailysudokuservice.accessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

import javax.inject.Singleton;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.ghoulean.dailysudokuservice.constants.Environment;
import com.ghoulean.sudoku.Puzzle;

import lombok.NonNull;

@Singleton
public final class DynamoDbAccessor {

    private @NonNull final DynamoDB dynamoDB;
    private @NonNull final Table table;

    private static final String CONDITION_EXPRESSION = "attribute_not_exists(puzzledate)";

    public DynamoDbAccessor() {
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        this.dynamoDB = new DynamoDB(client);
        this.table = dynamoDB.getTable(Environment.getTableName());
    }

    public void putItemInTable(final LocalDateTime date, final Puzzle puzzle) {
        final Item item = new Item()
            .withPrimaryKey("puzzledate", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            .withString("puzzle", puzzleToString(puzzle))
            .withInt("version", 1);

        final PutItemOutcome outcome = table.putItem(item, CONDITION_EXPRESSION, null, null);

        return;
    }

    private String puzzleToString(final Puzzle puzzle) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(puzzle);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            return "IOEXCEPTION";
        }
    }
}
