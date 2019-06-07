package org.repocrud.repository;

import org.repocrud.config.SecurityUtils;
import org.repocrud.domain.Glossary;
import org.repocrud.domain.Language;
import org.repocrud.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GlossaryRepository extends JpaRepository<Glossary, Long>, JpaSpecificationExecutor<Glossary> {


    Glossary findByLanguageAndKey(Language language, String key);

    default String getText(String key) {
        Language locale = getLanguage();
        Glossary byLanguageAndKey = findByLanguageAndKey(locale, key);
        return (byLanguageAndKey != null) ? byLanguageAndKey.getValue() : null;
    }

    default Language getLanguage() {
        Language locale= Language.RUSSIAN;
        try {
            User user = (User) SecurityUtils.getUserDetails();

            locale = (user != null)? user.getLocale() : Language.RUSSIAN;
        } catch (Exception e) {
            //do nothing
        }
        return locale;
    }

}
