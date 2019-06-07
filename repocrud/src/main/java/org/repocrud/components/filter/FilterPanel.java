package org.repocrud.components.filter;

import org.repocrud.domain.Filter;
import org.repocrud.repository.FilterRepository;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Denis B. Kulikov<br/>
 * date: 19.11.2018:14:22<br/>
 */
@Slf4j
public class FilterPanel<T> extends Composite<Div> {

    private final Class<T> domainType;

    private final FilterRepository repository;

    private final Button add;
    private List<FilterComponent> filterComponents;
    private final HorizontalLayout container;
//    private final List<>

    public FilterPanel(Class<T> domainType, FilterRepository repository) {
        this.domainType = domainType;
        this.repository = repository;
        container = new HorizontalLayout();
        this.filterComponents = new ArrayList<FilterComponent>();
        List<FieldWrapper> wrappers = Stream.of(domainType.getDeclaredFields())
                .filter(field -> field.getType().isAssignableFrom(String.class)
                                || field.getType().isAssignableFrom(Number.class)
                                || field.getType().isAssignableFrom(ZonedDateTime.class)
                        //|| field.getType().isAssignableFrom()

                ).map(FieldWrapper::new).collect(Collectors.toList());

        load(wrappers);

        add = new Button(VaadinIcon.SEARCH_PLUS.create());
        add.addClickListener(event -> {
            FilterComponent filterComponent = new FilterComponent(new Filter(), wrappers);

            container.add(new Removable(filterComponent));

        });

        Button save = new Button(VaadinIcon.STORAGE.create());
        save.addClickListener(event -> save());
        getContent().add(container, save);
    }

    private void save() {
        List<Filter> toRemoveFilters = new ArrayList<>((int) container.getChildren().count());
        List<Filter> toUpateFilters = new ArrayList<>((int) container.getChildren().count());
        container.getChildren()
                .map(component -> ((Removable) component))
                .forEach(c -> {
                    Filter filter = c.filterComponent.getFilter();
                    if (c.isVisible()
                            && isNotEmpty(filter.getEntity(), filter.getField(), filter.getValue())
                            && filter.getOperation() != null) {
                        toUpateFilters.add(filter);
                    } else {
                        toRemoveFilters.add(filter);
                    }
                });

        log.info("Deleting filters {}", toRemoveFilters);
        repository.deleteAll(toRemoveFilters);


        log.info("Save filters {}", toRemoveFilters);
        repository.saveAll(toUpateFilters);
    }

    private boolean isNotEmpty(String... values) {
        return Stream.of(values).allMatch(StringUtils::isNotEmpty);
    }


    private boolean load(List<FieldWrapper> wrappers) {
        List<Filter> filters = repository.findByEntityOrderByPositionAsc(domainType.getName());

        if (filters.size() == 0) {
            return false;
        }
        filters.stream()
                .map(filter -> new FilterComponent(filter, wrappers))
                .forEach(filterComponent -> this.getContent().add(filterComponent));
        return true;
    }


    private class Removable extends HorizontalLayout {

        private final FilterComponent filterComponent;

        private Removable(FilterComponent filterComponent) {
            this.filterComponent = filterComponent;
            Button button = new Button(VaadinIcon.CLOSE_SMALL.create());
            button.addClickListener(event -> this.setVisible(false));
            add(filterComponent);
        }
    }


}
