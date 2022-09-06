package com.sersolutions.doxis4helpers.commons;

import com.ser.blueline.ISession;
import com.ser.blueline.metaDataComponents.IDictionary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class for getting translated text
 */
public class Translation {
    static boolean isInit=false;
    static IDictionary[] dictionaries;
    static ConcurrentMap<String, IDictionary> dictionaryConcurrentMap;

    /**
     * Load translation dictionaries from Doxis4
     * @param doxis4session Doxis4 Session object
     *                      @see ISession
     * @param reInitIfAlreadyInitialized Boolean that means do we need to reload dictionary or not
     */
    public static void Init(ISession doxis4session, boolean reInitIfAlreadyInitialized) {
        if (isInit && !reInitIfAlreadyInitialized) return;
        dictionaries = doxis4session.getDocumentServer().getMetaDataConnector(doxis4session).getDictionaries();
        dictionaryConcurrentMap = new ConcurrentHashMap<>(dictionaries.length);
        for (IDictionary dictionary : dictionaries) {
            dictionaryConcurrentMap.put(dictionary.getLanguageID(), dictionary);
        }

        isInit = true;
    }

    /**
     * Modes of working of getting translation (what must be returned if no value found)
     */
    public enum GetTranslationModeIfNotFound {
        RETURN_ORIGINAL,
        RETURN_EMPTY,
        RETURN_NULL,
        EXCEPTION
    }

    /**
     * Get translated text from dictionary by Language code
     * @param languageID Language code (1033 for example)
     * @param originalText Text that must be translated
     * @param mode Mode of working when no value found
     * @return Translated text
     * @throws Exception if "Exception" mode was selected and no values was found
     */
    public static String getTranslationFromDicitonary(String languageID, String originalText, GetTranslationModeIfNotFound mode) throws Exception {
        if (!isInit) throw new Exception("Translation was not initialized!");
        if (!dictionaryConcurrentMap.containsKey(languageID)) {
            switch (mode) {
                case RETURN_ORIGINAL: return originalText;
                case RETURN_EMPTY: return "";
                case RETURN_NULL: return null;
                case EXCEPTION: throw new Exception(String.format("Can't find language by ID '%s'", languageID));
            }
        }
        IDictionary dictionary = dictionaryConcurrentMap.get(languageID);
        String translation =dictionary.getTranslation(originalText);
        if (translation == null) {
            switch (mode) {
                case RETURN_ORIGINAL:
                    translation = originalText;
                    break;
                case RETURN_EMPTY:
                    translation = "";
                    break;
                case EXCEPTION:
                    throw new Exception(String.format("Can't find translation for text '%s' in language by ID '%s'", originalText, languageID));
            }
        }
        return translation;
    }
}
