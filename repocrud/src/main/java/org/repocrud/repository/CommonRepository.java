package org.repocrud.repository;

import org.repocrud.annotations.CheckBoxCollection;
import org.repocrud.service.ApplicationContextProvider;
import org.hibernate.collection.internal.PersistentBag;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Denis B. Kulikov<br/>
 * date: 14.03.2019:17:53<br/>
 */
@NoRepositoryBean
public interface CommonRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {


    default <E> Collection<E> lazyLoadEnumList(Collection<E> t, CheckBoxCollection checkBoxCollection) {
        if (t instanceof PersistentBag && ! ApplicationContextProvider.getEmf().getPersistenceUnitUtil().isLoaded(t)) {
            Serializable key = ((PersistentBag) t).getKey();
            String foreignKey = checkBoxCollection.foreignKey();
            List all = this.findAll((Specification) (root, cq, cb) -> cb.equal(root.get(foreignKey), key));
            AtomicReference<Method> reference = new AtomicReference<>();
            Object collect = all.stream()
                    .map(o -> {
                        Method method = reference.get();
                        if (method == null) {
                            method = BeanUtils.getPropertyDescriptor(o.getClass(), checkBoxCollection.valueField()).getReadMethod();
                            reference.set(method);
                        }
                        return enumValue(o, method);
                    })
                    .collect(Collectors.toList());

            t = (List<E>) collect;
        }
        return t;
    }


    default Object enumValue(Object o, Method readMethod) {
        try {
            return readMethod.invoke(o);
        } catch (IllegalAccessException|InvocationTargetException e) {

            throw new IllegalStateException(e);
        }
    }

}
