package org.repocrud.crud;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.Collection;


/**
 * @author Denis B. Kulikov<br/>
 * date: 13.09.2018:18:46<br/>
 */
public class ListReadField extends AbstractField<ListReadField, Collection> {

    public ListReadField(Collection defaultValue) {
        super(defaultValue);



    }

    @Override
    protected void setPresentationValue(Collection ts) {


    }
}

