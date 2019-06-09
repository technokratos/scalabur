package org.repocrud.crud;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.domain.Courier;
import org.repocrud.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Slf4j
@UIScope
@SpringComponent
public class CourierCrudContainer extends AbstractCrudContainer<Courier, Long> {

    @Autowired
    private CourierRepository repository;


    @PostConstruct
    private void init() {
        crud = new RepositoryCrud<>(Courier.class, repository);


        getContent().add(crud);
    }
}
