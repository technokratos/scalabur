package org.repocrud.service;

import org.repocrud.crud.RepositoryCrud;

import java.util.List;

/**
 * @author Denis B. Kulikov<br/>
 * date: 16.09.2018:21:07<br/>
 */
public interface CrudFactory {

    <T,ID> RepositoryCrud<T, ID> createFactory(Class<T> type);

    <T,ID> RepositoryCrud<T, ID> createFactoryWithShow(Class<T> type, List<String> showField);

    <T,ID> RepositoryCrud<T, ID> createFactoryWithHide(Class<T> type, boolean readOnly, String... hideField);
}
