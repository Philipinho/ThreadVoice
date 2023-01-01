package TTS_Engine;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import utils.LanguageDetection;
import utils.ReadProperty;

import java.io.*;

public class GoogleTTS {

    public static String processSpeech(String speechText, String language, String filename) {
        String outputFilename = null;


        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(ReadProperty.getValue("gtt.config")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FixedCredentialsProvider fcp = FixedCredentialsProvider.create(credentials);

        TextToSpeechSettings speechSettings = null;
        try {
            speechSettings = TextToSpeechSettings.newBuilder().
                    setCredentialsProvider(fcp).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(speechSettings)) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(speechText)
                    .build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            language = LanguageDetection.select(language);

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(language)
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
                    audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Write the response to the output file.

            outputFilename = filename + ".mp3";

            try (OutputStream out = new FileOutputStream(ReadProperty.getValue("voice.path") + outputFilename)) {

                out.write(audioContents.toByteArray());

                System.out.println("Audio content written to file " + outputFilename + " - (GTT)");
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        return outputFilename;
    }

}
