package org.repocrud.crud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

import java.util.Optional;

/**
 * @author Denis B. Kulikov<br/>
 * date: 27.02.2019:21:13<br/>
 */
public abstract class AbstractCrudContainer<T,ID> extends Composite<Div> implements RefreshableComponent {
    protected RepositoryCrud<T, ID> crud;

    @Override
    public void refresh() {
        if (crud != null) {
            crud.refresh();
        }
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && parentTabVisible(getElement().getParent());
    }

    public static boolean parentTabVisible(Element parent) {
        if (parent == null) {
            return true;
        } else {
            Optional<Component> parentComponent = parent.getComponent();
            if (parentComponent.isPresent() &&
                    RefreshableComponent.class.isAssignableFrom(parent.getComponent().get().getClass())) {
                return parentComponent.get().isVisible();

            } else {
                return parentTabVisible(parent.getParent());
            }
        }
    }
}
