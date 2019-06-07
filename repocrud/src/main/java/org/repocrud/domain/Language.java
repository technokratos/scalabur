package org.repocrud.domain;

import java.util.Locale;

/**
 * @author Denis B. Kulikov<br/>
 * date: 21.09.2018:22:25<br/>
 */
public enum Language {
    RUSSIAN("Русский", new Locale("ru", "RU"));

    private final String language;
    private final Locale locale;

    Language(String language, Locale locale) {
        this.language = language;
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

}
