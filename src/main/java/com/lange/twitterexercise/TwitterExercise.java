package com.lange.twitterexercise;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.lange.common.dbc.DesignByContract;
import com.lange.common.functional.Pair;
import com.lange.common.rest.TokenStore;
import com.lange.twitterexercise.events.AuthorizationEvent;
import com.lange.twitterexercise.events.QueryFriendsEvent;
import com.lange.twitterexercise.events.QueryTweetsEvent;
import com.lange.twitterexercise.models.Tweet;
import com.lange.twitterexercise.models.UserInfo;
import com.lange.twitterexercise.operations.Oauth;
import com.lange.twitterexercise.operations.OperationQueue;
import com.lange.twitterexercise.orchestrators.QueryFriendsOrchestrator;
import com.lange.twitterexercise.orchestrators.QueryTweetsOrchestrator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.lange.common.dbc.DesignByContract.*;

/**
 * Created by lange on 22/1/16.
 */
public class TwitterExercise implements DesignByContract.Ensurable<TwitterExercise> {

    private static final Logger LOGGER = Logger.getLogger(TwitterExercise.class.getName());

    final Pair<String, String> consumerKeySecret;
    final String twitterHandle;
    final EventBus bus;
    final LocalDate toDate;

    final Map<String, UserInfo> friendsCache;
    final Map<LocalDate, Map<String, Long>> tweetCount;

    final QueryFriendsOrchestrator queryFriendsOrchestrator;
    final QueryTweetsOrchestrator queryTweetsOrchestrator;

    final AtomicInteger queryCompletionCount = new AtomicInteger(0);

    private TwitterExercise(String consumerKey, String consumerSecret, String twitterHandle, LocalDate toDate) {
        super();
        this.consumerKeySecret = Pair.create(consumerKey, consumerSecret);
        this.twitterHandle = twitterHandle;
        this.bus = new EventBus();
        this.friendsCache = Collections.synchronizedMap(Maps.newHashMap());
        this.tweetCount = Collections.synchronizedMap(Maps.newHashMap());
        this.toDate = toDate;

        bus.register(this);

        queryFriendsOrchestrator = QueryFriendsOrchestrator.create(bus, twitterHandle);

        queryTweetsOrchestrator = QueryTweetsOrchestrator.create(bus, toDate);

    }

    public static TwitterExercise create(String consumerKey, String consumerSecret, String twitterHandle, LocalDate toDate) {
        requireNotEmpty(consumerKey);
        requireNotEmpty(consumerSecret);
        requireNotEmpty(twitterHandle);
        requireNotNull(toDate);
        TwitterExercise exercise = new TwitterExercise(consumerKey, consumerSecret, twitterHandle, toDate);
        return ensureContracts(exercise);
    }

    public void start() {
        TokenStore.getInstance().setConsumerKeyAndSecretFromSystemProperties();
        boolean bearerTokenReceived = Oauth.create(consumerKeySecret).execute();
        validate(bearerTokenReceived, x -> x ? Optional.empty() : Optional.of("Error during authorization"));
        if (!bearerTokenReceived) {
            bus.post(AuthorizationEvent.createFailed());
        } else {
            bus.post(AuthorizationEvent.createSuccess());
        }
    }

    @Subscribe
    public void onEvent(AuthorizationEvent.AuthorizationFailedEvent event) {
        LOGGER.log(Level.INFO, "AUTHORIZATION FAILED!!!!\n\n");
        System.exit(-1);
    }

    @Subscribe
    public void onEvent(AuthorizationEvent.AuthorizationSuccessEvent event) {
        LOGGER.log(Level.INFO, "AUTHORIZATION SUCCESS!!!!\n\n");
        queryFriendsOrchestrator.queryFriends();
    }

    @Subscribe
    public void onEvent(QueryFriendsEvent.QueryFriendsCompletedEvent event) {
        queryCompletionCount.incrementAndGet();

        LOGGER.log(Level.INFO, String.format("QueryFriendsCompletedEvent. Count [%s]", queryCompletionCount.get()));

        LOGGER.log(Level.INFO, String.format("Friends Query Completed. Queried [%s] friends.", event.getFriendsList().size()));
        event.getFriendsList().forEach(x -> friendsCache.put(x.userId, x));
        queryTweetsOrchestrator.queryTweets(event.getFriendsList());

    }

    @Subscribe
    public void onEvent(QueryFriendsEvent.QueryFriendsFailedEvent event) {
        queryCompletionCount.incrementAndGet();

        LOGGER.log(Level.INFO, String.format("QueryFriendsFailedEvent. Count [%s]", queryCompletionCount.get()));

        LOGGER.log(Level.INFO, String.format("Friends Query Returned Error: ", event.getException()));
    }

    @Subscribe
    public void onEvent(QueryTweetsEvent.QueryTweetsCompletedEvent event) {
        queryCompletionCount.incrementAndGet();

        LOGGER.log(Level.INFO, String.format("QueryTweetsCompletedEvent. Count [%s]", queryCompletionCount.get()));

        LOGGER.log(Level.INFO, String.format("Batch Tweet Query Completed: %s", event.tweetsList.size()));

        Map<LocalDate, Map<String, Long>> batchMap = classifyByDateAndUserId(event.tweetsList);

        for (Map.Entry<LocalDate, Map<String, Long>> entry : batchMap.entrySet()) {
            if (tweetCount.containsKey(entry.getKey())) {
                // merge
                Map<String, Long> existingUserIdTweetCountMap = tweetCount.get(entry.getKey());
                Map<String, Long> batchUserIdTweetCountMap = entry.getValue();

                for (Map.Entry<String, Long> userIdCountEntry : batchUserIdTweetCountMap.entrySet()) {
                    if (existingUserIdTweetCountMap.containsKey(userIdCountEntry.getKey())) {
                        //merge
                        existingUserIdTweetCountMap.put(userIdCountEntry.getKey(),
                                existingUserIdTweetCountMap.get(userIdCountEntry.getKey()) + userIdCountEntry.getValue());
                    } else {
                        existingUserIdTweetCountMap.put(userIdCountEntry.getKey(), userIdCountEntry.getValue());
                    }

                }

            } else {
                tweetCount.put(entry.getKey(), Collections.synchronizedMap(entry.getValue()));
            }
        }

        LOGGER.log(Level.INFO, "Merged.");

        if (queryCompletionCount.get() == OperationQueue.getEnqueueCount().get()) {
            finished();
        }
    }

    @Subscribe
    public void onEvent(QueryTweetsEvent.QueryTweetsFailedEvent event) {

        queryCompletionCount.incrementAndGet();

        LOGGER.log(Level.INFO, String.format("QueryTweetsFailedEvent. Count [%s]", queryCompletionCount.get()));

        LOGGER.log(Level.INFO, String.format("Batch Tweet Query Returned Error: ", event.getException()));
    }

    private void finished() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n\n\n==========\nTWEET STATISTICS\n");

        tweetCount.entrySet().forEach(perDateEntry -> {
            buffer.append("\n\tDATE: ").append(perDateEntry.getKey());
            perDateEntry.getValue().entrySet().forEach(perUserIdEntry -> {
                buffer.append(String.format("\n\t\tUser (id: %s, handle: %s) [count: %s] ",
                        perUserIdEntry.getKey(),
                        friendsCache.get(perUserIdEntry.getKey()).screenName,
                        perUserIdEntry.getValue()));
            });
        });

        buffer.append("\n=========\n\n\n");

        LOGGER.log(Level.INFO, buffer.toString());

        System.exit(0);

    }

    public Map<LocalDate, Map<String, Long>> classifyByDateAndUserId(List<Tweet> tweetsList) {
        return tweetsList.stream()
                .collect(Collectors.groupingBy(Tweet::getDate))
                .entrySet().stream()
                // Map<LocalDate, List<Tweet>>
                .map(x -> Pair.create(
                        x.getKey(),
                        x.getValue().stream()
                                .collect(Collectors.groupingBy(Tweet::getUserId))))
                // Pair<LocalDate, Map<String, List<Tweet>>>
                .map(y -> Pair.create(
                        y.getLeft(),
                        y.getRight().entrySet().stream()
                                .map(z -> Pair.create(z.getKey(), (long) z.getValue().size()))
                                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public TwitterExercise ensurePostConditionContracts() {
        requireNotNull(consumerKeySecret);
        requireNotNull(bus);
        requireNotEmpty(twitterHandle);
        requireNotNull(queryFriendsOrchestrator);
        requireNotNull(toDate);
        return this;
    }
}
