package TTS_Engine;

import com.amazonaws.util.IOUtils;
import okhttp3.*;
import data.MicrosoftVoice;
import utils.LanguageDetection;
import utils.ReadProperty;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MicrosoftTTS {
    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final String apiUrl = "https://eastus.tts.speech.microsoft.com/cognitiveservices/v1";
    private static final String tokenUrl = "https://eastus.api.cognitive.microsoft.com/sts/v1.0/issuetoken";
    private static final String voiceList = "https://eastus.tts.speech.microsoft.com/cognitiveservices/voices/list";
    private static final String apiKey = ReadProperty.getValue("microsoft.key");


    public static String processSpeech(String speechText, String language, String filename) {
        String outputFilename = null;
        String voiceId = "";

        switch (language){
            case "en": voiceId = randomEnglishVoice();
                break;
            case "fr": voiceId = MicrosoftVoice.frenchHortenseRUS;
                break;
            case "es": voiceId = MicrosoftVoice.spanishHelenaRUS;
                break;
            case "pt": voiceId = MicrosoftVoice.portugueseHeloisaRUS;
                break;
            case "gl": voiceId = MicrosoftVoice.portugueseHeloisaRUS;
                break;
            case "de": voiceId = MicrosoftVoice.germanHeddaRUS;
                break;
            case "it": voiceId = MicrosoftVoice.italianLuciaRUS;
                break;
            case "tr": voiceId = MicrosoftVoice.turkishSedaRUS;
                break;
            case "ru": voiceId = MicrosoftVoice.russianEkaterinaRUS;
                break;
            default: voiceId = MicrosoftVoice.englishJessaNeural;
                break;
        }

        language = LanguageDetection.select(language);

        String gender = "Male";
        String rawSsml = "<speak version='1.0' xml:lang='%s'><voice xml:lang='%s' xml:gender='%s' name='%s'>%s</voice></speak>";

        String ssmlData = String.format(rawSsml,language,language,gender,voiceId,speechText);

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", getBearerToken());
        headers.put("Content-Type","application/ssml+xml");
        headers.put("X-Microsoft-OutputFormat","audio-16khz-32kbitrate-mono-mp3");
        headers.put("User-Agent", ReadProperty.getValue("site.domain"));

        Headers requestHeaders = Headers.of(headers);

        RequestBody requestBody = RequestBody.create(ssmlData.getBytes());

        Request voiceRequest = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .headers(requestHeaders)
                .post(requestBody)
                .build();

        try {
            Response response = okHttpClient.newCall(voiceRequest).execute();

            if (response.isSuccessful()) {
                outputFilename = filename + ".mp3";

                InputStream inputStream = response.body().byteStream();

                FileOutputStream outputStream = new FileOutputStream(ReadProperty.getValue("voice.path") + outputFilename);
                IOUtils.copy(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
                System.out.println("Audio content written to file " + outputFilename + " - (MSFT-TTS)");

            } else {
                System.out.println(response.code() + " - MSFT");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return outputFilename;

    }

    private static String randomEnglishVoice(){
        switch (new Random().nextInt(5)){
            case 0: return MicrosoftVoice.englishGuyRus;
            case 1: return MicrosoftVoice.englishJessaRUS;
            case 2: return MicrosoftVoice.englishBenjaminRUS;
            case 3: return MicrosoftVoice.englishJessaNeural;
            case 4: return MicrosoftVoice.englishZiraRUS;
            default: return MicrosoftVoice.englishJessaRUS;
        }
    }

    private static String getBearerToken() {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Ocp-Apim-Subscription-Key",apiKey).build();

        Request tokenRequest = new Request.Builder().url(tokenUrl).post(requestBody)
                .addHeader("Ocp-Apim-Subscription-Key",apiKey).build();

        try {
            Response response = okHttpClient.newCall(tokenRequest).execute();

            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String getVoiceList() {
        Request tokenRequest = new Request.Builder().url(voiceList)
                .addHeader("Authorization","Bearer " + getBearerToken()).build();
        try {
            Response response = okHttpClient.newCall(tokenRequest).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
