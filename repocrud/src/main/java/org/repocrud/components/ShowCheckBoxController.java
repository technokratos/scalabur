package org.repocrud.components;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;

import java.util.stream.Stream;

/**
 * @author Denis B. Kulikov<br/>
 * date: 02.12.2018:14:28<br/>
 */
public class ShowCheckBoxController extends Composite<Div> {

    private final Checkbox showBox;
    private final Component[] components;


    public ShowCheckBoxController(String text, Component... components) {
        this.showBox = new Checkbox(text);
        this.components = components;
        setVisibleComponents(false);
        this.showBox.addValueChangeListener(event ->
                setVisibleComponents(event.getValue()));
        getContent().add(showBox);
    }

    public void setVisibleComponents(Boolean value) {
        Stream.of(components).forEach(component -> component.setVisible(value));
    }

    public Registration addValueChangeListener(
            HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        return showBox.addValueChangeListener(listener);
    }
}
