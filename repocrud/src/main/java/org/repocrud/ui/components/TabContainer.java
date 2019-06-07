package org.repocrud.ui.components;

import org.repocrud.crud.RefreshableComponent;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import java.util.*;

public class TabContainer extends Composite<Div> implements RefreshableComponent{

    private final Tabs.Orientation orientation;
    private final Tabs tabs;
    private final HasComponents layout;
    private final Map<String, Component> map = new HashMap<>();
    private List<ComponentEventListener<Tabs.SelectedChangeEvent>> listeners = new ArrayList<>();

    private TabContainer parentTab = null;
    private final Set<RefreshableComponent> onceRefreshed;


    public TabContainer(Tabs.Orientation orientation) {
        tabs = new Tabs();
        onceRefreshed = new HashSet<>();

        tabs.setOrientation(orientation);
        this.orientation = orientation;
        layout = orientation == Tabs.Orientation.VERTICAL ? new HorizontalLayout() : new VerticalLayout();
        Tabs tabs = this.tabs;
        layout.add(tabs);
        tabs.addSelectedChangeListener(selectedChangeEvent -> {
            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab != null) {
                String label = selectedTab.getId().orElse(selectedTab.getLabel());
                map.values().stream().filter(Component::isVisible).forEach(component -> component.setVisible(false));
                Component component = map.get(label);
                if (component != null) {
                    component.setVisible(true);
                }
                if (listeners != null) {
                    listeners.forEach(l -> l.onComponentEvent(selectedChangeEvent));
                }

                if (component instanceof RefreshableComponent && !onceRefreshed.contains(component)) {
                    ((RefreshableComponent) component).refresh();
                    onceRefreshed.add((RefreshableComponent) component);
                }
            }
        });
        getContent().add((Component) layout);


    }

    public void addSelectedChangeListener(ComponentEventListener<Tabs.SelectedChangeEvent> listener) {
        listeners.add(listener);
    }

    public Tab addTab(String title, Component component) {
        if (map.isEmpty()) {
            component.setVisible(true);
        } else {
            component.setVisible(false);
        }
        Tab tab = new Tab(title);
        //todo on the last tab and tested tabs the grid overlays the header, without it
        component.getElement().getStyle().set("padding", "20px");
        //tab.setLabel(title);
        addTab(component, tab);
        return tab;
    }

    private void addTab(Component component, Tab tab) {
        String id = "tab" + map.size();
        tab.setId(id);
        tabs.add(tab);
        map.put(id, component);
        layout.add(component);
    }

    public void removeAll() {
        tabs.removeAll();
        map.clear();
    }

    public void addTab(VaadinIcon icon, String title, TabContainer chileTab) {
        addTab(icon, title, (Component)chileTab);
        chileTab.setParentTab(this);
    }
    public void addTab(VaadinIcon icon, String title, Component component) {
        if (map.isEmpty()) {
            component.setVisible(true);
        } else {
            component.setVisible(false);
        }

        Tab tab = new Tab(icon.create(), new Label(title));

        //tab.setLabel(title);
        addTab(component, tab);
        return;
    }

    @Override
    public boolean isVisible() {
        return (parentTab == null && super.isVisible()) || (parentTab != null && parentTab.isVisible() && super.isVisible());
    }

    private void setParentTab(TabContainer parentTab) {
        this.parentTab = parentTab;
    }

    @Override
    public void refresh() {
        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab != null) {
            String label = selectedTab.getId().orElse(selectedTab.getLabel());
            Component component = map.get(label);
            if (component instanceof RefreshableComponent && !onceRefreshed.contains(component)) {
                ((RefreshableComponent) component).refresh();
                onceRefreshed.add((RefreshableComponent) component);
            }
        }
    }
}

