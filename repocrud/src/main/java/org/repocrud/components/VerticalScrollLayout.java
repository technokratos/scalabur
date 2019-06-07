package org.repocrud.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VerticalScrollLayout extends VerticalLayout {
    private VerticalLayout content;

    public VerticalScrollLayout(){
        preparePanel();
    }

    public VerticalScrollLayout(Component... children){
        preparePanel();
        this.add(children);
    }

    private void preparePanel() {
        setWidth("100%");
        setHeight("100%");
        getStyle().set("overflow", "auto");

        content = new VerticalLayout();
        content.getStyle().set("display", "block");
        content.setWidth("100%");
        content.setPadding(false);
        super.add(content);
    }

    public VerticalLayout getContent(){
        return content;
    }

    @Override
    public void add(Component... components){
        content.add(components);
    }

    @Override
    public void remove(Component... components){
        content.remove(components);
    }

    @Override
    public void removeAll(){
        content.removeAll();
    }

    @Override
    public void addComponentAsFirst(Component component) {
        content.addComponentAtIndex(0, component);
    }
}