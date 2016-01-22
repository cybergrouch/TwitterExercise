package com.lange.twitterexercise;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by lange on 22/1/16.
 */
public class Main {
    public static void main(String... args) {
        System.setProperty("consumer.key", "<<consumer key>>");
        System.setProperty("consumer.secret", "<<consumer secret>>");
        System.setProperty("twitter.handle", "<<twitter handle>>");
        System.setProperty("twitter.date.to", LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        TwitterExercise exercise = TwitterExercise.create(
                System.getProperty("consumer.key"),
                System.getProperty("consumer.secret"),
                System.getProperty("twitter.handle"),
                LocalDate.parse(System.getProperty("twitter.date.to")));
        exercise.start();
    }
}
