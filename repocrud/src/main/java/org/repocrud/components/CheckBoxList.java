package org.repocrud.components;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Denis B. Kulikov<br/>
 * date: 16.09.2018:11:43<br/>
 */
public class CheckBoxList<T extends Identifiable> extends Composite<Div> {

    private List<T> allItems;
    private final VerticalLayout container;
    private List<Checkbox> checkboxes = Collections.emptyList();
    private List<HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>> listeners = new ArrayList<>();
    private final Class<T> type;


    public CheckBoxList(List<T> allItems, String title, Class<T> type) {
        super();
        this.type = type;

        this.allItems = allItems;


        //add(new Label(title));
        container = new VerticalLayout();
        initAll(allItems);

//        container.add(checkboxGroup);
    }

    public void initAll(List<T> allItems) {
        allItems.stream().map(identified -> {
            Checkbox checkbox = new Checkbox(identified.toString());
            checkbox.setId(identified.getId().toString());
            listeners.forEach(checkbox::addValueChangeListener);
            return checkbox;
        }).forEach(container::add);
        getContent().add(container);
    }

    public void setAllItems(List<T> allItems) {
        this.allItems = allItems;
        container.removeAll();
        initAll(allItems);
    }

    public void setSelected(List<T> selectItems) {
//        CheckboxGroup<T>  checkboxGroup = new CheckboxGroup<>();
//
//        checkboxGroup.setItems(this.allItems);
//        Set<T> unselect = allItems.stream().filter(a -> !selectItems.contains(a)).collect(Collectors.toSet());
//
//        checkboxGroup.updateSelection(new HashSet<>(selectItems), unselect);
        Set<String> set = selectItems.stream().map(identified -> identified.getId().toString()).collect(Collectors.toSet());
        container.getChildren().forEach(component -> {
                ((Checkbox) component).setValue(set.contains(component.getId().orElse("")));

        });
    }

    public void addChangeValueList(HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>> listener){
        checkboxes.forEach(checkbox -> checkbox.addValueChangeListener(listener));
        listeners.add(listener);
    }

    public Set<Long> getSelected() {
        return container.getChildren()
                .filter(component -> component instanceof Checkbox)
                .filter(component -> ((Checkbox) component).getValue())
                .map(component -> component.getId().orElse(""))
                .filter(Objects::nonNull)

                .map(Long::parseLong).collect(Collectors.toSet());

    }
}
