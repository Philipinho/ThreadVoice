import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetV2;
import utils.Utils;

import java.util.*;

public class ThreadReader {

    private static final TwitterClient twitter = Utils.twitterClient();

    public static List<Tweet> formatThread(List<Tweet> tweets) {
        List<Tweet> tweetList = new ArrayList<>();
        tweetList.add(tweets.get(0));

        List<Tweet> filtered = new ArrayList<>();
        String replyIdForNextTweet = tweetList.get(0).getId();
        List<Tweet> upperTweets = readUpperTweets(replyIdForNextTweet, tweets.get(0).getUser().getName());


        for (int i = 1; i < tweets.size(); i++) {

            if (tweets.get(i).getInReplyToStatusId() != null){
                if (tweets.get(i).getInReplyToStatusId().equals(replyIdForNextTweet)) {
                    replyIdForNextTweet = tweets.get(i).getId();
                    filtered.add(tweets.get(i));
                }
            }
        }

        filtered.addAll(0, tweetList);
        if (upperTweets != null && upperTweets.size() > 0) {
            filtered.addAll(0, upperTweets);
        }

        return filtered;
    }

    public static List<Tweet> getTweetSinceId(String tweetId) {
        Tweet status;
        List<Tweet> tweets = new ArrayList<>();

        try{
            status = twitter.getTweet(tweetId);

            List<TweetV2.TweetData> timeline = new ArrayList<>();

            AdditionalParameters additionalParameters = AdditionalParameters.builder()
                    .sinceId(tweetId)
                    .maxResults(100).build();

            timeline = twitter.getUserTimeline(status.getAuthorId(), additionalParameters).getData();


            Collections.sort(timeline, new Comparator<Tweet>() {
                @Override
                public int compare(Tweet tweet1, Tweet tweet2) {
                    return tweet1.getId().compareTo(tweet2.getId());
                }
            });

            tweets.addAll(0, timeline);

            tweets.add(0, status);

        } catch (Exception e){
            e.printStackTrace();
        }

        return tweets;
    }

    public static List<Tweet> readUpperTweets(String lastId, String screenName) {
        Tweet tweet = null;
        List<Tweet> tweets = new ArrayList<>();

        try{
            tweet = twitter.getTweet(lastId);
            String id = "";

            if (tweet.getInReplyToStatusId() != null) {
                do {
                    id = tweet.getInReplyToStatusId();
                    if (id != null) {
                        tweet = twitter.getTweet(id);
                    }

                    if (tweet.getUser().getName().equalsIgnoreCase(screenName)) {
                        tweets.add(tweet);
                    }
                } while (tweet.getInReplyToStatusId() != null
                        && tweet.getUser().getName().equalsIgnoreCase(screenName));
            }

            Collections.reverse(tweets);

        } catch (Exception e){
            e.printStackTrace();
        }

        return tweets;
    }

}
