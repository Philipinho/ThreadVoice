package TTS_Engine;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.util.IOUtils;
import data.AmazonVoice;
import utils.LanguageDetection;
import utils.ReadProperty;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class AmazonTTS {
    private static AmazonPolly polly;

    public static String processSpeech(String speech, String language, String filename) {
        String accessKey = ReadProperty.getValue("aws.accesskey");
        String secretKey = ReadProperty.getValue("aws.secretkey");
        String voiceId = "";

        AmazonTTS voice = new AmazonTTS(accessKey,secretKey);

        switch (language){
            case "en": voiceId = randomEnglishVoice();
            break;
            case "fr": voiceId = randomFrenchVoice();
            break;
            case "es": voiceId = randomSpanishVoice();
            break;
            case "pt": voiceId = randomPortugueseVoice();
            break;
            case "gl": voiceId = AmazonVoice.portugueseCamila;
            break;
            case "de": voiceId = randomGermanVoice();
            break;
            case "it": voiceId = AmazonVoice.italianBianca;
            break;
            case "tr": voiceId = AmazonVoice.turkishFiliz;
            break;
            case "ru": voiceId = AmazonVoice.russianMaxim;
            break;
          //  case "arb": voiceId = AmazonVoice.arabicZeina;
            //break;
            default: voiceId = AmazonVoice.englishJoana;
            break;
        }

        language = LanguageDetection.select(language);

        InputStream voiceStream = voice.synthesizeAudioStream(speech,language,voiceId);

        String outputFilename = filename + ".mp3";

        try {
            FileOutputStream outputStream = new FileOutputStream(ReadProperty.getValue("voice.path") + outputFilename);

            try {
                IOUtils.copy(voiceStream,outputStream);
                voiceStream.close();
                outputStream.close();

                System.out.println("Audio content written to file " + outputFilename + " - (AMZ - TTS)");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return outputFilename;

    }

    public static String randomEnglishVoice(){
        switch (new Random().nextInt(4)){
            case 0: return AmazonVoice.englishJoana;
            case 1: return AmazonVoice.englishMatthew;
            case 2: return AmazonVoice.englishJoey;
            case 3: return AmazonVoice.englishSalli;
            default: return AmazonVoice.englishJoana;
        }
    }

    private static String randomSpanishVoice(){
        switch (new Random().nextInt(2)){
            case 0: return AmazonVoice.spanishConchita;
            case 1: return AmazonVoice.spanishEnrique;
            default: return AmazonVoice.spanishConchita;
        }
    }

    private static String randomFrenchVoice(){
        switch (new Random().nextInt(3)){
            case 0: return AmazonVoice.frenchCeline;
            case 1: return AmazonVoice.frenchLea;
            case 2: return AmazonVoice.frenchMathieu;
            default: return AmazonVoice.frenchLea;
        }
    }

    private static String randomGermanVoice(){
        switch (new Random().nextInt(2)){
            case 0: return AmazonVoice.germanHans;
            case 1: return AmazonVoice.germanMarlene;
            default: return AmazonVoice.germanHans;
        }
    }

    private static String randomPortugueseVoice(){
        switch (new Random().nextInt(3)){
            case 0: return AmazonVoice.portugueseCamila;
            case 1: return AmazonVoice.portugueseRicardo;
            case 2: return AmazonVoice.portugueseVitoria;
            default: return AmazonVoice.portugueseCamila;
        }
    }


    public AmazonTTS(String accessKey, String secretKey) {

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);

        AmazonPollyClientBuilder clientBuilder = AmazonPollyClientBuilder.standard();
        polly = clientBuilder.withCredentials(provider).withRegion(Regions.US_EAST_1).build();
    }

    public InputStream synthesizeAudioStream(String text, String language, String voiceId) {

        SynthesizeSpeechRequest request = new SynthesizeSpeechRequest();
        request.withText(text).withLanguageCode(language).withVoiceId(voiceId).withOutputFormat(OutputFormat.Mp3);

        SynthesizeSpeechResult result = polly.synthesizeSpeech(request);
        return result.getAudioStream();
    }
}
