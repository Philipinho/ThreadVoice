package utils;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.vdurmont.emoji.EmojiParser;
import data.Language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LanguageDetection {

    public static boolean isSupported(String language){
        String[] supportedLanguages= {"en","fr","es","tr","pt","gl","de","ru","it"};
        List<String> languageList = Arrays.asList(supportedLanguages);

        for (String lang : languageList){
            if (lang.equals(language))
                return true;
        }

        return false;
    }

    public static String getLanguage(String content) {
        StringBuilder response = new StringBuilder();
        List<LanguageProfile> languageProfiles = null;

        try {
            languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

        TextObject textObject = textObjectFactory.forText(EmojiParser.removeAllEmojis(content));

      //  System.out.println(textObject);
        Optional<LdLocale> langDetected = languageDetector.detect(textObject);

        if (langDetected.isPresent()) {
            response.append(langDetected.get());
        } else {
            response.append("Not detected");
        }

        return response.toString();
    }

    public static String select (String language){
        switch (language){
            case "en": return Language.english;
            case "fr": return Language.french;
            case "es": return Language.spanish;
            case "pt": return Language.portuguese;
            case "gl": return Language.portuguese;
            case "de": return Language.german;
            case "tr": return Language.turkish;
            case "it": return Language.italian;
            case "ru": return Language.russian;
          //  case "ar": return Language.arabic;
            default:   return Language.english;

        }
    }
}