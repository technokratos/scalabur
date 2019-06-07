package org.repocrud.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class Wrap {

    public static Component icon(VaadinIcon icon, Component component) {
        return new HorizontalLayout(icon.create(), component);
    }
}
