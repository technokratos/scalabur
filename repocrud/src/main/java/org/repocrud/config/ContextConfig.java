package org.repocrud.config;

import org.repocrud.service.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Denis B. Kulikov<br/>
 * date: 23.11.2018:12:44<br/>
 */
@Configuration
public class ContextConfig {

    @Autowired
    private ApplicationContext context;

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        ApplicationContextProvider provider = new ApplicationContextProvider();
        provider.setApplicationContext(context);
        return provider;
    }
}
