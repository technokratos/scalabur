package org.repocrud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Service
public class GenericRepository {

    @Autowired
    private ApplicationContext appContext;

    Repositories repositories = null;

    @PostConstruct
    public void init() {
        repositories = new Repositories(appContext);
    }

    public JpaRepository getRepository(AbstractPersistable entity) {
        return getRepository(entity.getClass());
    }
    public JpaRepository getRepository(Class aClass) {
        Optional<Object> repositoryFor = repositories.getRepositoryFor(aClass);
        if(!repositoryFor.isPresent()) {
            log.error("Not found repository for {}", aClass);
            throw new IllegalArgumentException("Not found repository for " + aClass);
        }
        return (JpaRepository) repositoryFor.orElse(null);
    }

    public Object save(AbstractPersistable entity) {
        return getRepository(entity).save(entity);
    }

    public Object findAll(AbstractPersistable entity) {
        return getRepository(entity).findAll();
    }

    public void delete(AbstractPersistable entity) {
        getRepository(entity).delete(entity);
    }
}