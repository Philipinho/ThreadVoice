package TTS_Engine;

import com.voicerss.tts.AudioCodec;
import com.voicerss.tts.AudioFormat;
import com.voicerss.tts.VoiceParameters;
import com.voicerss.tts.VoiceProvider;
import utils.LanguageDetection;
import utils.ReadProperty;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class VoiceRSS {

    public static String processSpeech(String speechText,  String language, String filename) {

        if (language.equals("tr")){
            if (speechText.length() < 2999){
              return randomizeTurkish(speechText,language,filename);
            }
            return GoogleTTS.processSpeech(speechText,language,filename);
        }

        language = LanguageDetection.select(language);

        VoiceProvider voiceProvider = new VoiceProvider(ReadProperty.getValue("voicerss.key"));
        VoiceParameters vp = new VoiceParameters(speechText, language);
        vp.setCodec(AudioCodec.MP3);
        vp.setFormat(AudioFormat.Format_44KHZ.AF_44khz_16bit_mono);
        vp.setBase64(false);
        vp.setSSML(false);
        vp.setRate(0);

        byte[] speech = null;

        try {
            speech = voiceProvider.speech(vp);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String outputFilename = filename + ".mp3";

        try {
            FileOutputStream fos = new FileOutputStream(ReadProperty.getValue("voice.path") + outputFilename);
            try {
                fos.write(speech, 0, speech.length);
                fos.close();

                System.out.println("Audio content written to file " + outputFilename + " - (VRSS)");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return outputFilename;
    }

    public static String processSpeech(String speechText, String filename) {
        String outputFilename = null;

        VoiceProvider voiceProvider = new VoiceProvider(ReadProperty.getValue("voicerss.key"));
        VoiceParameters vp = new VoiceParameters( speechText, "en-US");
        vp.setCodec(AudioCodec.MP3);
        vp.setFormat(AudioFormat.Format_22KHZ.AF_22khz_16bit_stereo);
        vp.setBase64(false);
        vp.setSSML(false);
        vp.setRate(0);

        byte[] speech = null;

        try {
            speech  = voiceProvider.speech(vp);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        outputFilename = filename + ".mp3";

        try {
            FileOutputStream fos = new FileOutputStream(ReadProperty.getValue("voice.path") + outputFilename);
            try {
                fos.write(speech,0,speech.length);
                fos.close();
                System.out.println("Audio content written to file " + outputFilename + " - (VRSS)");

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return outputFilename;
    }

    // The Turkish language is not available on VoiceRSS, when it gets called, we delegate the request to other providers.
    public static String randomizeTurkish(String threadContent,String language, String voiceFilename){
        switch (new Random().nextInt(2)){
            case 0: return GoogleTTS.processSpeech(threadContent, language,voiceFilename);
            case 1: return AmazonTTS.processSpeech(threadContent, language, voiceFilename);
          //  case 2: return MicrosoftTTS.processSpeech(threadContent, language, voiceFilename);
            default: return GoogleTTS.processSpeech(threadContent, language,voiceFilename);
        }
    }
}
