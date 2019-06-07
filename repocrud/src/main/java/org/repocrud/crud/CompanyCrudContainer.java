package org.repocrud.crud;

import org.repocrud.config.SecurityUtils;
import org.repocrud.repository.CompanyRepository;
import org.repocrud.domain.Company;
import org.repocrud.domain.User;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@UIScope
@SpringComponent
public class CompanyCrudContainer extends AbstractCrudContainer<Company, Long> {

    @Autowired
    private CompanyRepository repository;

    private final User userDetails = (User) SecurityUtils.getUserDetails();

    @PostConstruct
    private void init() {
        crud = new RepositoryCrud<>(Company.class, repository);

        getContent().add(crud);

        if (userDetails.getCompany() != null) {

            crud.setFindAllOperation(() -> Arrays.asList(userDetails.getCompany()));
            crud.setPageCountSupplier(() -> 1L );
        }
    }

}
