package org.repocrud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;

@Slf4j
//@Component
public class ApplicationContextProvider implements ApplicationContextAware{

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    private static Repositories repositories = null;

    private static EntityManagerFactory emf;

    public static JpaRepository getRepository(Class aClass) {
        Optional<Object> repositoryFor = repositories.getRepositoryFor(aClass);
        if(!repositoryFor.isPresent()) {
            log.error("Not found repository for {}", aClass);
            throw new IllegalArgumentException("Not found repository for " + aClass);
        }
        return (JpaRepository) repositoryFor.orElse(null);
    }

    @Override
    public void setApplicationContext(ApplicationContext ac)
            throws BeansException {
        context = ac;
        repositories = new Repositories(ac);
        emf = context.getBean(EntityManagerFactory.class);
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }
}