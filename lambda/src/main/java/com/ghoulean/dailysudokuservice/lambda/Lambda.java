package com.ghoulean.dailysudokuservice.lambda;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.ghoulean.dailysudokuservice.accessor.DynamoDbAccessor;
import com.ghoulean.dailysudokuservice.constants.Environment;
import com.ghoulean.sudoku.Puzzle;
import com.ghoulean.sudoku.generators.Standard3x3Generator;

import lombok.NonNull;

public final class Lambda implements RequestStreamHandler {

    private @NonNull final Standard3x3Generator standard3x3Generator;
    private @NonNull final Calendar calendar;
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;

    public Lambda() {
        this.standard3x3Generator = new Standard3x3Generator();
        this.calendar = Calendar.getInstance();
        this.dynamoDbAccessor = new DynamoDbAccessor();
    }

    @Override
    public void handleRequest(@NonNull final InputStream inputStream,
                              @NonNull final OutputStream outputStream,
                              @NonNull final Context context) {
        final LambdaLogger lambdaLogger = context.getLogger();
        lambdaLogger.log("Begin running DailySudokuService");

        final LocalDateTime today = LocalDateTime.now()
            .withNano(0)
            .withSecond(0)
            .withMinute(0)
            .withHour(0);
        final LocalDateTime tomorrow = today.plusDays(1);

        lambdaLogger.log(String.format("Generating puzzle. Current date: %s; tomorrow: %s",
            today.toString(),
            tomorrow.toString()));

        final ExecutorService executor = Executors.newCachedThreadPool();
        final Callable<Puzzle> task = new Callable<Puzzle>() {
            public Puzzle call() {
                return standard3x3Generator.generate();
            }
        };

        for (int i = 0; i < Environment.getNumberRetries() + 1; i += 1) {
            final Future<Puzzle> future = executor.submit(task);
            try {
                final Puzzle newPuzzle = future.get(10, TimeUnit.MINUTES);
                lambdaLogger.log("Successfully generated new puzzle");
                lambdaLogger.log("Putting the following in table: " + newPuzzle.getStartingBoard().toString());
                this.dynamoDbAccessor.putItemInTable(tomorrow, newPuzzle);
                break;
            } catch (ExecutionException e) {
                lambdaLogger.log(String.format("Attempt %d / %d failed to generate puzzle",
                    i + 1,
                    Environment.getNumberRetries() + 1));
                continue;
            } catch (TimeoutException e) {
                lambdaLogger.log(String.format("Attempt %d / %d timed out trying to generate puzzle",
                i + 1,
                Environment.getNumberRetries() + 1));
            } catch (InterruptedException e) {
                lambdaLogger.log(String.format("Attempt %d / %d interrupted trying to generate puzzle",
                i + 1,
                Environment.getNumberRetries() + 1));
            }
        }

        lambdaLogger.log("Done running DailySudokuService");
        return;
    }
}
