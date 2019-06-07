package org.repocrud.crud;

import org.repocrud.domain.Glossary;
import org.repocrud.repository.GlossaryRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author Denis B. Kulikov<br/>
 * date: 29.09.2018:18:53<br/>
 */
@Slf4j
@UIScope
@SpringComponent
public class GlossaryCrudContainer extends AbstractCrudContainer<Glossary, Long> {

    @Autowired
    private GlossaryRepository repository;

    @PostConstruct
    private void init() {

        RepositoryCrudFormFactory<Glossary> formFactory = new RepositoryCrudFormFactory<>(Glossary.class);

        crud = new RepositoryCrud<>(repository, formFactory);
        crud.setSortProperties(new String[]{"key"});
        getContent().add(crud);
    }

}
