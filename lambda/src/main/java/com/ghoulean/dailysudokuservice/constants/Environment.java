package com.ghoulean.dailysudokuservice.constants;

import com.amazonaws.regions.Regions;

import lombok.NonNull;

public abstract class Environment {

    public static Regions getAwsRegion() {
        return Regions.fromName(getEnv("AWS_REGION"));
    }

    public static String getTableName() {
        return getEnv("TABLE_NAME");
    }

    public static int getNumberRetries() {
        try {
            return Integer.parseInt(getEnv("RETRIES"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static String getEnv(@NonNull final String key) {
        return System.getenv(key);
    }
}
