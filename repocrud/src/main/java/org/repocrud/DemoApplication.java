package org.repocrud;

import lombok.extern.slf4j.Slf4j;
import org.hsqldb.Server;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;

/**
 * @author Denis B. Kulikov<br/>
 * date: 18.03.2019:9:50<br/>
 */
@Slf4j
@EnableJpaAuditing
@SpringBootApplication
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        MultipartAutoConfiguration.class,// excluded so that the application uses commons-fileupload instead of Servlet 3 Multipart support
        FlywayAutoConfiguration.class
})
@EntityScan(basePackages = {"org.repocrud.domain"})
@EnableJpaRepositories(basePackages = {"org.repocrud.repository"})
public class DemoApplication implements ApplicationRunner {

    public static void main(String[] args) {
        server();
        SpringApplication.run(DemoApplication.class, args);

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        log.info("NonOptionArgs: {}", args.getNonOptionArgs());
        log.info("OptionNames: {}", args.getOptionNames());

        for (String name : args.getOptionNames()) {
            log.info("arg-" + name + "=" + args.getOptionValues(name));
        }

        boolean containsOption = args.containsOption("person.name");
        log.info("Contains person.name: " + containsOption);
    }

    public static Server server() {
        Server server = new Server();
        server.setDatabaseName(0, "mainDb");
        server.setDatabasePath(0, "mem:mainDb");
        server.setDatabaseName(1, "standbyDb");
        server.setDatabasePath(1, "mem:standbyDb");
        int port = 9001;
        server.setPort(port); // this is the default port
        server.start();
        // log.info("Start HSQL server {}:{}", port, "mainDb" );
        return server;
    }
}
