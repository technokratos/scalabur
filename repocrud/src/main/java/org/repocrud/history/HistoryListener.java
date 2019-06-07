
package org.repocrud.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.config.SecurityUtils;
import org.repocrud.domain.CrudHistory;
import org.repocrud.domain.User;
import org.repocrud.repository.CrudHistoryRepository;
import org.repocrud.service.ApplicationContextProvider;
import org.repocrud.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Slf4j
@Component
public class HistoryListener {

    @Autowired
    private CrudHistoryRepository repository;



    private Executor executor = Executors.newFixedThreadPool(4);

    private ThreadLocal<ObjectMapper> objectMapperThreadLocal = new ThreadLocal<>();

    @PrePersist
    public void prePersist(Auditable ob) {
        User userDetails = (User) SecurityUtils.getUserDetails();
        ZonedDateTime now = ZonedDateTime.now();
        ob.createdBy = userDetails;
        ob.createdDate = now;
        ob.lastModifiedBy = userDetails;
        ob.lastModifiedDate = now;
        if (userDetails != null && userDetails.getCompany() != null) {
            ob.setCompany(userDetails.getCompany());
        }
    }

    @PostPersist
    public void postPersist(Auditable ob) {
        User user = (User) SecurityUtils.getUserDetails();
        addHistory(CrudHistory.Operation.SAVE, user, ob);

    }

    @PostLoad
    public void postLoad(Auditable ob) {

    }

    @PreUpdate
    public void preUpdate(Auditable ob) {
        ob.lastModifiedBy = (User) SecurityUtils.getUserDetails();
        ob.lastModifiedDate = ZonedDateTime.now();
    }

    @PostUpdate
    public void postUpdate(Auditable ob) {
        User user = (User) SecurityUtils.getUserDetails();
        addHistory(CrudHistory.Operation.UPDATE, user, ob);

    }

    @PreRemove
    public void preRemove(Auditable ob) {
        System.out.println("Listening Auditable Pre Remove : ");
    }

    @PostRemove
    public void postRemove(Auditable ob) {
        User user = (User) SecurityUtils.getUserDetails();
        addHistory(CrudHistory.Operation.DELETE, user, ob);
    }


    public void addHistory(CrudHistory.Operation operation, User user, Object arg) {
        executor.execute(() -> {
            try {
                String type = arg.getClass().getSimpleName();

                String body = getBody(arg);
                CrudHistory crudHistory = new CrudHistory(null, user, ZonedDateTime.now(), type, operation,
                        body);
                ApplicationContextProvider.getRepository(CrudHistory.class).saveAndFlush(crudHistory);
            } catch (Exception e) {
                log.error("Error in add history", e);
            }
        });
    }

    private String getBody(Object object) {

        ObjectMapper objectMapper = objectMapperThreadLocal.get();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new Hibernate5Module());
            objectMapperThreadLocal.set(objectMapper);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        }
        try {

            String s = objectMapper.writeValueAsString(object);
            return (s.length() >= 1000) ?
                    s.substring(0, 1000) :
                    s;

        } catch (JsonProcessingException e) {
            log.error("Error in serializtion by jaxson ", e);
        }
        return "";
    }

} 