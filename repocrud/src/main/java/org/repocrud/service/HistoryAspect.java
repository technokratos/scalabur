package org.repocrud.service;


import org.repocrud.config.SecurityUtils;
import org.repocrud.domain.CrudHistory;
import org.repocrud.domain.User;
import org.repocrud.history.Auditable;
import org.repocrud.repository.CrudHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Aspect
@Component
public class HistoryAspect {

    private ConcurrentMap<Class, JAXBContext> contextConcurrentMap = new ConcurrentHashMap<>();

    private ThreadLocal<ConcurrentMap<Class, Marshaller>> marshallerLocalMap = new ThreadLocal<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectWriter objectWriter = objectMapper.writer();
    private ThreadLocal<ObjectMapper>  objectMapperThreadLocal = new ThreadLocal<>();

    @Autowired
    private CrudHistoryRepository repository;

    private Executor executor = Executors.newFixedThreadPool(4);


//    @Pointcut("execution(* *.saveAndFlush(*)) && " +
//            "!args(org.repocrud.domain.CrudHistory)")
////            "!execution(* org.repocrud.repository.CrudHistoryRepository.saveAndFlush(*))")
//    public void commonSave() {
//    }

//    @Pointcut("execution(* org.springframework.data.repository.CrudRepository.delete(*))" +
//            " || execution(* org.springframework.data.repository.CrudRepository.deleteAll(*))" +
//            " || execution(* org.springframework.data.repository.CrudRepository.deleteById(*))")
//    public void commonDelete() {
//    }


//    @Around("commonSave()")
//    public Object addCommonData(final ProceedingJoinPoint pjp) throws Throwable {
//
//        Class<?> target = pjp.getArgs()[0].getClass();
//
//        return proceed(pjp, CrudHistory.Operation.SAVE, target);
//    }
//
//    @Around("commonDelete()")
//    public Object delete(final ProceedingJoinPoint pjp) throws Throwable {
//        Class<?> target = pjp.getArgs().length>0 ? pjp.getArgs()[0].getClass() : null;
//        return proceed(pjp, CrudHistory.Operation.DELETE, target);
//    }

    @Pointcut("execution(* org.springframework.security.authentication.AuthenticationProvider.authenticate(*))")
    public void login() {
    }

    @Pointcut("execution(* org.repocrud.service.LogoutSuccessHandler.onLogoutSuccess(*))")
    public void logout() {
    }

    @Around("login()")
    public Object addLoginHistory(final ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Object proceed;
        try {
            proceed = pjp.proceed(args);

            executor.execute(() -> {
                AbstractAuthenticationToken token = (AbstractAuthenticationToken) proceed;
                User user = (User) token.getPrincipal();
                CrudHistory.Operation operation;
                if (token instanceof RememberMeAuthenticationToken) {
                    operation = CrudHistory.Operation.REMBER_LOGIN;
                } else {
                    operation = token.isAuthenticated() ? CrudHistory.Operation.LOGIN : CrudHistory.Operation.LOGOUT;
                }
                addAction(user, operation);
            });

        } catch (Throwable t) {
            throw t;
        }
        return proceed;
    }

    public void addAction(User user, CrudHistory.Operation login) {
        CrudHistory crudHistory = new CrudHistory(null, user, ZonedDateTime.now(), User.class.getSimpleName(), login,
                null);
        repository.saveAndFlush(crudHistory);
    }

    @Around("logout()")
    public Object addLogoutHistory(final ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Object proceed;
        try {
            User user = (User) SecurityUtils.getUserDetails();
            proceed = pjp.proceed(args);
            executor.execute(() -> {
//                Authentication authentications = (Authentication) args[3];
                //User user = (User) authentications.getPrincipal();
//                user.setCrudHistories(Collections.emptyList());
                addAction(user, CrudHistory.Operation.LOGOUT);
            });

        } catch (Throwable t) {
            throw t;
        }
        return proceed;
    }


    public Object proceed(ProceedingJoinPoint pjp, CrudHistory.Operation operation, Class target) throws Throwable {
        Object[] args = pjp.getArgs();
        Object proceed;
        try {
            proceed = pjp.proceed(args);

            if (Auditable.class.isAssignableFrom(target)
                    || (Collection.class.isAssignableFrom(target)
                    && args.length> 0
                    && ((Collection)args[0]).size() > 0
                    && Auditable.class.isAssignableFrom(((Collection)args[0]).iterator().next().getClass()))) {
                User user = (User) SecurityUtils.getUserDetails();
                Object arg = args[0];
                String type;
                if (Collection.class.isAssignableFrom(target)) {
                    type = ((Collection)args[0]).iterator().next().getClass().getSimpleName();
                } else {
                    type = target.getSimpleName();
                }
                executor.execute(() -> {
                    addHistory(operation, user, arg, type);
                });
            }
        } catch (Throwable t) {
            throw t;
        }
        return proceed;
    }

    public void addHistory(CrudHistory.Operation operation, User user, Object arg, String type) {
        String body = getBody(arg);
        CrudHistory crudHistory = new CrudHistory(null, user, ZonedDateTime.now(), type, operation,
                body);
        repository.saveAndFlush(crudHistory);
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