package org.repocrud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.domain.CrudHistory;
import org.repocrud.domain.User;
import org.repocrud.repository.CrudHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Denis B. Kulikov<br/>
 * date: 22.04.2019:19:59<br/>
 */
@Slf4j
@Service
public class HistoryService {

    @Autowired
    private CrudHistoryRepository repository;

    private Executor executor = Executors.newFixedThreadPool(4);

    private ThreadLocal<ObjectMapper> objectMapperThreadLocal = new ThreadLocal<>();

    public void addHistory(CrudHistory.Operation operation, @NonNull User user, Object arg) {
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
