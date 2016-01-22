# TwitterExercise

This application takes a customer key, customer secret, and a Twitter handler and acquires a application-only token from Twitter. Using this application-only bearer token, it queries for friends associated with the Twitter handler passed and counts the tweets from these friends across a 7 days. It tabulates these findings per day per user.

To use the application, you need to set the following system parameters:
* customer.key
* customer.secret
* twitter.handle

I've conveniently put in placeholders in the code where one could easily put in these values. Please see the following files:
* com/lange/twitterexercise/Main.java
* com/lange/twitterexercise/operations/BaseOperationsTest.java (for running unit tests)

These lines are exposed:
```
     ... {
        ...
        System.setProperty("customer.key", "<<customer key>>");
        System.setProperty("customer.secret", "<<customer secret>>");
        System.setProperty("twitter.handle", "<<twitter handle>>");
        ...
    }
```

After changing the above lines, the application can be run issuing the following command:

```
$ cd <<project directory>>
$ ./gradlew run
```













