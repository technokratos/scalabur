package org.repocrud.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Denis B. Kulikov<br/>
 * date: 27.03.2019:23:11<br/>
 */
@Configuration
@ComponentScan(value = {"org.repocrud.service", "org.repocrud.crud"})
@Import(value = {DataSourceConfig.class, SecurityConfig.class, ContextConfig.class, ErrorPageFilterConfig.class, PasswordConfig.class})
public class RepocrudConfig {
}
