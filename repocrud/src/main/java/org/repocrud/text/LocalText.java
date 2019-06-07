package org.repocrud.text;

import org.repocrud.domain.Glossary;
import org.repocrud.domain.Language;
import org.repocrud.repository.GlossaryRepository;
import org.repocrud.service.ApplicationContextProvider;
import com.vaadin.flow.component.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
public class LocalText {

    private static Map<Pair<String, Locale>, String> map = new ConcurrentHashMap<>();

    private static final GlossaryRepository GLOSSARY_REPOSITORY;

    static {

        GlossaryRepository repository = null;
        try {
            repository = (GlossaryRepository) ApplicationContextProvider.getRepository(Glossary.class);
            List<Glossary> all = repository.findAll();
            all.forEach(glossary -> {
                put(glossary.getKey(), glossary.getValue(), glossary.getLanguage());
            });
        } catch (Exception e) {
            log.error("Error in load glossary repository", e);
        }

        GLOSSARY_REPOSITORY = repository;

    }
    //    ResourceBundle bundle = ResourceBundle.getBundle("repocrud", Locale.getDefault(), new UTF8Control());
    //    private static ResourceBundle resources = ResourceBundle.getBundle("repocrud", Locale.getDefault());
    private static ResourceBundle resources = ResourceBundle.getBundle("repocrud", Locale.getDefault(), new UTF8Control());


    public static String text(Class domain, String key, Object... params) {
        return getResourceWithDefaultValue(domain.getSimpleName() + "." + key, params, () -> key);
    }

    public static String text(Class domain, Class nested, String key, Object... params) {
        return getResourceWithDefaultValue(domain.getSimpleName() + "." + nested.getSimpleName() + "." + key, params, () -> key);
    }

    /**
     * domain.key
     * domain.form.key
     */
    public static String text(String key, Object... params) {
        return getResourceWithDefaultValue(key, params, () -> key);
    }

    public static String getResourceWithDefaultValue(String key, Object[] params, Supplier<String> defaultSupplier) {
        Locale locale = LocaleTools.getUILocale();
        return getResourceWithDefaultValue(key, params, defaultSupplier, locale);
    }

    public static String text(String key, Locale locale, Object... params) {
        return getResourceWithDefaultValue(key, params, () -> key, locale);
    }

    public static String getResourceWithDefaultValue(String key, Object[] params, Supplier<String> defaultSupplier, Locale locale) {
        Pair<String, Locale> localePair = Pair.of(key, locale);
        String text = map.get(localePair);
        if (text == null) {
            final String resourceText;
            if (GLOSSARY_REPOSITORY != null) {
                resourceText = getTextFormRepository(key, defaultSupplier);

            } else {
                resourceText = getTextFromProperties(key, defaultSupplier);
            }
            map.put(localePair, resourceText);

            return (params != null && params.length >0 ) ? format(params, resourceText) : resourceText;
        } else {
            return (params != null && params.length >0) ? format(params, text) : text;
        }
    }

    private static String format(Object[] params, String resourceText) {
        try {
            if (resourceText.contains("%")) {
                return String.format(resourceText, params);
            } else {
                return resourceText + " " + Arrays.toString(params);
            }
        } catch (Exception e) {
            log.error("Invalid format " + resourceText, e);
        }
        return resourceText + " " + Arrays.toString(params);
    }

    public static String getTextFormRepository(String key, Supplier<String> defaultSupplier) {
        String resourceText;
        resourceText = GLOSSARY_REPOSITORY.getText(key);
        if (resourceText == null) {
            resourceText = defaultSupplier.get();
            Glossary glossary = new Glossary(null, GLOSSARY_REPOSITORY.getLanguage(), key, resourceText);
            GLOSSARY_REPOSITORY.saveAndFlush(glossary);
        }
        return resourceText;
    }

    public static String getTextFromProperties(String key, Supplier<String> defaultSupplier) {
        String resourceText;
        try {
            resourceText = resources.getString(key);
        } catch (MissingResourceException e) {

            resourceText = defaultSupplier.get();
            log.info("{}=?", key);
        }
        return resourceText;
    }


    public static void put(String key, String value, Language language) {
        try {
            map.put(Pair.of(key, (language== null) ? Language.RUSSIAN.getLocale(): language.getLocale()), value);
        } catch (NullPointerException e) {
            log.error("Error in load keys {},{}, {}", key, value, language);
        }
    }

    static boolean save(Class domain, String key, String text) {
        if (GLOSSARY_REPOSITORY == null) {
            return false;
        }
        try {
            String formattedKey = domain.getSimpleName() + "." + key;
            Glossary glossary = GLOSSARY_REPOSITORY.findByLanguageAndKey(Language.RUSSIAN, formattedKey);
            if (glossary == null) {
                glossary = new Glossary(null, Language.RUSSIAN, formattedKey, text);
            } else {
                glossary.setValue(text);
            }
            GLOSSARY_REPOSITORY.saveAndFlush(glossary);
            put(formattedKey, text, Language.RUSSIAN);
            log.info("Add new key with help {}, {}, {}", domain.getSimpleName(), key, text);
            return true;
        } catch (Exception e) {
            log.error("Error in update key " + domain.getSimpleName() + " " + key, e);
            Notification.show(e.getMessage());
            return false;
        }

    }
}
