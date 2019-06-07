package org.repocrud.service;

import org.repocrud.crud.RepositoryCrud;
import org.repocrud.crud.RepositoryCrudFormFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Denis B. Kulikov<br/>
 * date: 16.09.2018:21:10<br/>
 */
@Service
public class CrudFactoryImpl implements CrudFactory {

    @Autowired
    Environment env;

    @Autowired
    private ApplicationContext context;



    @Override
    public <T, ID> RepositoryCrud<T, ID> createFactory(Class<T> type) {

        JpaRepository repository = ApplicationContextProvider.getRepository(type);

        RepositoryCrudFormFactory formFactory = new RepositoryCrudFormFactory(type);
        RepositoryCrud<T, ID> crud = new RepositoryCrud<>(repository, formFactory);
        return crud;
    }

    @Override
    public <T, ID> RepositoryCrud<T, ID> createFactoryWithShow(Class<T> type, List<String> showField) {
        JpaRepository repository = ApplicationContextProvider.getRepository(type);
        RepositoryCrudFormFactory formFactory = new RepositoryCrudFormFactory(type);
        formFactory.setVisibleProperties(showField.stream().toArray(String[]::new));
        RepositoryCrud<T, ID> crud = new RepositoryCrud<>(repository, formFactory);
        return crud;
    }


    @Override
    public <T, ID> RepositoryCrud<T, ID> createFactoryWithHide(Class<T> type, boolean readOnly, String... hideField) {
        JpaRepository repository = ApplicationContextProvider.getRepository(type);
        RepositoryCrudFormFactory formFactory = new RepositoryCrudFormFactory(type);

        formFactory.hideVisibleProperties(hideField);
        RepositoryCrud<T, ID> crud = new RepositoryCrud<>(repository, formFactory);
        if (readOnly) {
            crud.setAddOperationVisible(false);
            crud.setUpdateOperationVisible(false);
            crud.setDeleteOperationVisible(false);
        }
        return crud;
    }
}
