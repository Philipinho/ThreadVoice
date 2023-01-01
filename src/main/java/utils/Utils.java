package utils;

import com.vdurmont.emoji.EmojiParser;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;

import java.util.Objects;

public class Utils {

    public static String cleanThread(String tweets) {
        String matchLinks = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

        return EmojiParser.removeAllEmojis(tweets.toLowerCase()
                .replaceAll(matchLinks, "")
                .replaceAll("@threavoice audio", "")
                .replaceAll("@threadvoice record", ""));
    }

    public static TwitterClient twitterClient() {

        return new TwitterClient(TwitterCredentials.builder()
                .accessToken(ReadProperty.getValue("tw.accessToken"))
                .accessTokenSecret(ReadProperty.getValue("tw.accessTokenSecret"))
                .apiKey(ReadProperty.getValue("tw.apiKey"))
                .apiSecretKey(ReadProperty.getValue("tw.apiSecretKey"))
                .build());

    }
}