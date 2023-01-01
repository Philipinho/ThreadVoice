import TTS_Engine.MicrosoftTTS;
import com.vdurmont.emoji.EmojiParser;
import TTS_Engine.AmazonTTS;
import TTS_Engine.GoogleTTS;
import TTS_Engine.VoiceRSS;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.stream.StreamRules;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.dto.tweet.TweetType;
import utils.DBHelper;
import utils.LanguageDetection;
import utils.ReadProperty;

import utils.Utils;

import java.util.*;

import static utils.Utils.cleanThread;

public class Stream {

    private static final TwitterClient twitter = Utils.twitterClient();

    public static void main(String[] args) {

        String botUsername = ReadProperty.getValue("twitter.username");

        try{
            List<StreamRules.StreamRule> rules = twitter.retrieveFilteredStreamRules();

            if (rules == null){
                twitter.addFilteredStreamRule(botUsername, "");
            } else {
                for (StreamRules.StreamRule rule : rules) {
                    twitter.deleteFilteredStreamRuleId(rule.getId());
                }
                twitter.addFilteredStreamRule(botUsername, "");
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        twitter.startFilteredStream(new IAPIEventListener() {
            @Override
            public void onStreamError(int i, String s) {
                System.out.println(s);

            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                try {
                    String tweetReferencedId = "";
                    String mentionTweetId = tweet.getId();

                    String threadLanguage = null;

                    if (tweet.getTweetType().equals(TweetType.QUOTED)){
                        tweetReferencedId = tweet.getInReplyToStatusId();

                    } else if (tweet.getInReplyToStatusId() != null){
                        tweetReferencedId = tweet.getInReplyToStatusId();
                    }

                    if (tweetReferencedId != null) {

                        String whoMentionedMe = tweet.getUser().getName();

                        String outputFilename = null;

                        List<Tweet> tweetSince = ThreadReader.getTweetSinceId(tweetReferencedId);
                        List<Tweet> formatTweets = ThreadReader.formatThread(tweetSince);

                        StringBuilder threadContent = new StringBuilder();

                        if (formatTweets.size() >= 2) {
                            String firstTweetId = formatTweets.get(0).getId();
                            String threadAuthor = formatTweets.get(0).getUser().getName();
                            String voiceFilename = threadAuthor + "_" + firstTweetId;

                            boolean threadExist = DBHelper.isThreadExists(firstTweetId);

                            if (threadExist && !tweet.getText().toLowerCase().contains("refresh")) {
                                outputFilename = DBHelper.getThreadVoiceName(firstTweetId);

                            } else {

                                for (Tweet tweets : formatTweets) {
                                    threadContent.append(cleanThread(tweets.getText())).append("\n\n");
                                }

                                String threadSnippet = threadContent.toString().substring(0, Math.min(700, threadContent.toString().length()));
                                threadLanguage = LanguageDetection.getLanguage(threadSnippet);

                                //Detect language
                                if (!LanguageDetection.isSupported(threadLanguage)) {
                                    String notSupportedMessage = "Sorry, it is either the language on this thread is not yet supported was not detected.";
                                    replyTweet(notSupportedMessage, mentionTweetId, whoMentionedMe);

                                } else {

                                    threadContent.append("\n").append(credits(threadLanguage,EmojiParser.removeAllEmojis(formatTweets.get(0).getUser().getName())));

                                    // You can comment out a provider if you do not wish to use them. Processing decision is based on the character limits of the provider

                                    if (threadContent.length() < 2995) {
                                        outputFilename = AmazonTTS.processSpeech(threadContent.toString(), threadLanguage, voiceFilename);

                                    } else if(threadContent.length() < 4995) {
                                        outputFilename = GoogleTTS.processSpeech(threadContent.toString(), threadLanguage, voiceFilename);
                                    } else if(threadContent.length() < 9000){
                                        outputFilename = MicrosoftTTS.processSpeech(threadContent.toString(), threadLanguage, voiceFilename);
                                    }
                                    else {
                                        outputFilename = VoiceRSS.processSpeech(threadContent.toString(), threadLanguage, voiceFilename);
                                    }
                                }
                            }

                            if (outputFilename != null) {
                                String audioLink = ReadProperty.getValue("urlpath") + outputFilename;

                                replyTweet(tweetMessage(threadAuthor, audioLink), tweet.getId(), whoMentionedMe);

                                //save if thread doesn't exist
                                if (!threadExist) {
                                    String threadExcerpt = threadContent.substring(0, Math.min(280, threadContent.toString().length()));
                                    DBHelper.saveTweet(whoMentionedMe, mentionTweetId, firstTweetId, outputFilename, threadAuthor, threadExcerpt, threadLanguage);
                                }
                            } else {
                                String response = "Sorry, something went wrong while trying to process the audio. ";
                                replyTweet(response, mentionTweetId, whoMentionedMe);
                            }
                        } else {
                            String response = "Sorry, it is either this thread has less than 2 tweets or too old to be processed.";
                            //replyTweet(response, mentionTweetId, whoMentionedMe);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onUnknownDataStreamed(String s) {

            }

            @Override
            public void onStreamEnded(Exception e) {

            }
        });

    }

    private static String tweetMessage(String threadAuthor, String audioLink){

        String[] messages = {"Yes! This thread has been converted to voice version. You can listen or download it via ", "Hey, thread from @" +threadAuthor +  " is ready in audio format. You can listen or download it via: "};
        int rand = (int)(messages.length * Math.random());

        return messages[rand] + " " + audioLink;
    }

    private static void replyTweet(String text, String inReplyToTweetId, String whoMentionedMe) {

        TweetParameters tweetParams = TweetParameters.builder()
                .text(text)
                .reply(TweetParameters.Reply
                        .builder().inReplyToTweetId(inReplyToTweetId).build())
                .build();

        try{
            Tweet tweet = twitter.postTweet(tweetParams);
            System.out.println("Replied to: " + whoMentionedMe + " - Reply Id: " + tweet.getId());
        } catch (Exception e){
            e.printStackTrace();
        }



    }

    private static String credits(String language, String authorName){
        String domainName = ReadProperty.getValue("site.domain");

        switch (language){

            case "fr": return "Écrit par " + authorName +".\n Audio propulsé par " + domainName;

            case "es": return "Escrito por "+ authorName +".\n Audio impulsado por " + domainName;

            case "pt": return "Escrito por " + authorName +".\n Áudio desenvolvido por " + domainName;

            case "gl": return "Escrito por "+ authorName +".\n Audio impulsado por " + domainName;

            case "de": return "Geschrieben von "+ authorName +".\n Audio von " + domainName;

            case "tr": return "Yazan "+ authorName + "Ses, " + domainName + " tarafından desteklenmektedir.";

            case "it": return "Scritto da "+ authorName +".\n Audio alimentato da " + domainName;

            case "ru": return "Автор Иеремия " + authorName + " Аудио от " + domainName;

            default: return "Written by " + authorName + ".\n Audio Powered by " + domainName;
        }
    }

    /* we can randomize the TTS provider
    public static String randomTTSProvider(String threadContent, String voiceFilename, String language){
        switch (new Random().nextInt(2)){
            case 0: return GoogleTTS.processSpeech(threadContent, voiceFilename);
            case 1: return MicrosoftTTS.processSpeech(threadContent, language,voiceFilename);
            case 2: return AmazonTTS.processSpeech(threadContent, language, voiceFilename);
            default: return GoogleTTS.processSpeech(threadContent, voiceFilename);
        }
    }
     */
}
