package org.repocrud.components;

import org.repocrud.annotations.CheckBoxCollection;
import org.repocrud.repository.CommonRepository;
import org.repocrud.service.ApplicationContextProvider;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import org.hibernate.collection.internal.PersistentBag;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.repocrud.text.LocalText.text;

/**
 * @author Denis B. Kulikov<br/>
 * date: 14.03.2019:13:30<br/>
 */
public class EnumCollectionField<T extends Collection<E>, E extends Enum> extends HorizontalLayout
        implements HasValueAndElement<AbstractField.ComponentValueChangeEvent<EnumCollectionField<T, E>, T>, T> {


    private final TreeMap<E, Checkbox> boxMap = new TreeMap<E, Checkbox>();
    private final CheckBoxCollection checkBoxCollection;

    public EnumCollectionField(Class<E> type, CheckBoxCollection checkBoxCollection) {
        this.checkBoxCollection = checkBoxCollection;
        init(type);
    }

    private void init(Class<E> type) {
        Stream.of(type.getEnumConstants()).forEach(e -> {
            Checkbox checkbox = new Checkbox(text(type, e.toString()));
            add(checkbox);
            boxMap.put(e, checkbox);
        });
    }



    @Override
    public void setValue(T t) {
        boxMap.values().forEach(checkbox -> checkbox.setValue(false));
        if (t == null) {
            return;
        }
        CommonRepository repository = (CommonRepository) ApplicationContextProvider.getRepository(checkBoxCollection.type());
        t = (T) repository.lazyLoadEnumList(t, checkBoxCollection);
        //t = lazyLoadEnumList(t);

        t.stream().map(boxMap::get).forEach(cb-> cb.setValue(true));
    }

    private T lazyLoadEnumList(T t) {
        if (t instanceof PersistentBag && ! ApplicationContextProvider.getEmf().getPersistenceUnitUtil().isLoaded(t)) {
            Serializable key = ((PersistentBag) t).getKey();
            JpaSpecificationExecutor repository = (JpaSpecificationExecutor) ApplicationContextProvider.getRepository(checkBoxCollection.type());
            String foreignKey = checkBoxCollection.foreignKey();
            t = loadCollectionEnum(key, repository, foreignKey, checkBoxCollection.valueField());
        }
        return t;
    }

    private T loadCollectionEnum(Serializable key, JpaSpecificationExecutor repository, String foreignKey, String propertyName) {
        T t;
        List all = repository.findAll((Specification) (root, cq, cb) -> cb.equal(root.get(foreignKey), key));
        Object collect = all.stream()
                .map(o -> enumValue(o, propertyName))
                .collect(Collectors.toList());
        t = (T) collect;
        return t;
    }

    private Object enumValue(Object o, String propertyName) {
        try {
            return BeanUtils.getPropertyDescriptor(o.getClass(), propertyName).getReadMethod().invoke(o);
        } catch (IllegalAccessException|InvocationTargetException e) {

            throw new IllegalStateException(e);
        }
    }

    @Override
    public T getValue() {
        Collection<E> collect = boxMap.entrySet().stream().filter(entry -> entry.getValue().getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return (T) collect;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<EnumCollectionField<T, E>, T>> valueChangeListener) {
        List<Registration> registrations = boxMap.entrySet().stream().map(entry -> entry.getValue().addValueChangeListener(event -> {
            T value = getValue();
            if (event.getOldValue()) {
                value.add(entry.getKey());
            } else {
                value.remove(entry.getKey());
            }
            valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, value, event.isFromClient()));
        })).collect(Collectors.toList());
        return (Registration) () -> registrations.forEach(Registration::remove);
    }
}
