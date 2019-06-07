package org.repocrud.crud;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.annotations.NestedEntity;
import org.repocrud.annotations.NestedView;
import org.repocrud.history.Auditable;
import org.repocrud.history.Identified;
import org.repocrud.service.ApplicationContextProvider;
import org.repocrud.ui.components.TabContainer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.vaadin.crudui.layout.CrudLayout;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.repocrud.text.LocalText.text;

/**
 * @author Denis B. Kulikov<br/>
 * date: 14.09.2018:9:51<br/>
 */
@Slf4j
public class NestedTabs<T> extends Composite<Div> implements SelectionListener<Grid<T>, T> {


    private final TabContainer tabs = new TabContainer(Tabs.Orientation.HORIZONTAL);
    private final Class<T> parentType;
    private final List<Field> fieldStream;
    private final Map<Field, Consumer<ForiegnKey>> fieldSelectListeners = new HashMap<>();
    private final Map<String, RepositoryCrud> crudMap = new HashMap<>();

    public NestedTabs(Class<T> parentType, List<String> visibleProperties) {
        this.parentType = parentType;

        this.fieldStream = Stream.of(parentType.getDeclaredFields())
                .filter(field -> Collection.class.isAssignableFrom(field.getType()))
                .filter(field -> Stream.of(field.getAnnotations()).anyMatch(annotation -> annotation instanceof OneToMany))
                //.filter(field -> Stream.of(field.getAnnotations()).anyMatch(annotation -> annotation instanceof NestedEntity))
                .filter(field -> visibleProperties.contains(field.getName())).collect(Collectors.toList());
        getContent().add(tabs);

        fieldStream.forEach(field -> {
            OneToMany oneToMany = Stream.of(field.getAnnotations()).filter(annotation -> annotation instanceof OneToMany)
                    .map(annotation -> ((OneToMany) annotation)).findFirst().get();
            FetchType fetch = oneToMany.fetch();

            Optional<NestedEntity> optionalNestedEntity = Stream.of(field.getAnnotations()).filter(annotation -> annotation instanceof NestedEntity)
                    .map(annotation -> ((NestedEntity) annotation)).findFirst();
            //NestedEntity nestedEntity = (NestedEntity) optionalNestedEntity.get();

            Class nestedType = oneToMany.targetEntity();
            if (void.class == nestedType) {
                return;
            }
            //Class nestedType = nestedEntity.type();
            JpaRepository repository = ApplicationContextProvider.getRepository(nestedType);

            Field foreignField = getForeignField(nestedType);
            //ForiegnKey description = getNestedDescription(main, nestedType);


//            List<Component> nestedControls = getControls(nestedEntity, repository, description);


            final Component nested;
            if (!optionalNestedEntity.isPresent() || optionalNestedEntity.get().view() == NestedView.CRUD) {
                RepositoryCrudFormFactory crudFormFactory = new RepositoryCrudFormFactory<>(nestedType);
                crudFormFactory.hideVisibleProperties(foreignField.getName());
                ForiegnKey foriegnKey = new ForiegnKey(null, foreignField, null);
                RepositoryCrud crud = new RepositoryCrud(repository, crudFormFactory, foriegnKey);
                crudMap.put(field.getName(), crud);
                CrudLayout layout = crud.getCrudLayout();
                fieldSelectListeners.put(field, fKey -> {
                    crud.setForiegnKey(fKey);
                    crud.refreshGrid();
                });
                //nestedControls.forEach(layout::addToolbarComponent);

                nested = crud;
            } else {

                VerticalLayout verticalLayout = new VerticalLayout();
                HorizontalLayout controlLayouts = new HorizontalLayout();
                //nestedControls.forEach(controlLayouts::add);
                verticalLayout.add(controlLayouts);

                ListBox listBox = new ListBox();
//                listBox.setItems(repository.findAll(Example.of(description.getFilterButton())));
                verticalLayout.add(listBox);

                fieldSelectListeners.put(field, description -> listBox.setItems(saveFindByDescription(repository, description)));
                nested = verticalLayout;
            }

            tabs.addTab(text(nestedType, field.getName()), nested);
            tabs.setVisible(false);

        });
    }

    public RepositoryCrud getCrud(String property){
        return crudMap.get(property);
    }

    private List saveFindByDescription(JpaRepository repository, ForiegnKey description) {
        try {
            JpaSpecificationExecutor specificationExecutor = (JpaSpecificationExecutor) repository;
            return specificationExecutor.findAll((Specification) (e, cq, cb) -> cb.equal(e.get(description.getForeignField().getName()), description.getId()));
            //return repository.findAll(Example.of(description.getFilterButton()));
        } catch (Exception e) {
            log.error("Error in find nested entities " + description , e);
            return Collections.emptyList();
        }
    }

    @Override
    public void selectionChange(SelectionEvent<Grid<T>, T> selectionEvent) {

        Grid<T> grid = selectionEvent.getSource();
        tabs.setVisible(true);
        if (grid.getSelectedItems().size() == 1) {
            T main = grid.getSelectedItems().iterator().next();
//            updateAuditableFilter(main);
            tabs.setVisible(true);
            fieldStream.forEach(field -> {
//                NestedEntity nestedEntity = Stream.of(field.getAnnotations()).filter(annotation -> annotation instanceof NestedEntity)
//                        .map(annotation -> ((NestedEntity) annotation)).findFirst().get();
//                Class type = nestedEntity.type();
                OneToMany oneToMany = Stream.of(field.getAnnotations()).filter(annotation -> annotation instanceof OneToMany)
                        .map(annotation -> ((OneToMany) annotation)).findFirst().get();
                Class type = oneToMany.targetEntity();
                if (type != void.class) {
                    ForiegnKey description = getNestedDescription(main, type);
                    Consumer<ForiegnKey> foriegnKeyConsumer = fieldSelectListeners.get(field);
                    if (foriegnKeyConsumer != null) {
                        foriegnKeyConsumer.accept(description);
                    } else {
                        log.info("Not found nested ForiegnKey consumer for field {} for parent {}  ", field.getName(), main);
                    }
                }
            });

        }
    }

    private void updateAuditableFilter(T parent) {
        if (parent != null && parent instanceof Auditable) {
            Auditable auditable = (Auditable) parent;
            auditable.setCreatedBy(null);
            auditable.setLastModifiedBy(null);
//            auditable.setCompany(null);
        }

    }

//    private List<Component> getControls(NestedEntity entity, JpaRepository repository, ForiegnKey description, Class type) {
//        if (entity.add() == NestedAdd.NOTHING) {
//            return Collections.emptyList();
//        }
//
//        Button link = new Button(VaadinIcon.LINK.create());
//        Button unlink = new Button(VaadinIcon.UNLINK.create());
//        unlink.setEnabled(false);
//        //repository.findAll();
//        link.addClickListener(buttonClickEvent -> {
//            ComboBox comboBox = new ComboBox("", repository.findAll());
//            ConfirmDialog dialog = new ConfirmDialog(text(parentType, type, "header"),
//                    text(parentType, type, "text"),
//                    text(parentType, type, "confirm"),
//                    confirmEvent -> {
//                        Object value = comboBox.getValue();
//                        if (value != null) {
//                            description.initForeignKey(value);
//                            repository.saveAndFlush(value);
//                            log.info("Linked {}", value);
//                            Notification.show(text("linked"));
//                        }
//                    }
//            );
//            dialog.add(comboBox);
//            dialog.open();
//        });

//        unlink.addClickListener(buttonClickEvent -> {
//            ConfirmDialog dialog = new ConfirmDialog(text(parentType, entity.type(), "header"),
//                    text(parentType, entity.type(), "textUnlink"),
//                    text(parentType, entity.type(), "confirmUnlink"),
//                    confirmEvent -> {
//
//                        if (value != null) {
//                            description.resetForeignKey(value);
//                            repository.saveAndFlush(value);
//                            log.info("Linked {}", value);
//                            Notification.show(text("linked"));
//                        }
//                    }
//            );
//            dialog.open();
//        });
//
//        return Arrays.asList(link, unlink);
//    }

    public ForiegnKey getNestedDescription(T main, Class type) {
        try {
            Object exampleFilter = type.newInstance();
            //newInstance;;

            Field foreignField = getForeignField(type);

            foreignField.set(exampleFilter, main);
            Long id = main instanceof Identified ? ((Identified) main).getId(): null;
            return new ForiegnKey(exampleFilter, foreignField, id);
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error in new instance ", e);
            throw new IllegalStateException("Error in new instance", e);
        }
    }

    public Field getForeignField(Class type) {
        try {
            Field foreignField = type.getDeclaredField(getForeignKeyFieldName());
            if (!foreignField.isAccessible()) {
                foreignField.setAccessible(true);
            }
            return foreignField;
        } catch (NoSuchFieldException e) {
            log.error("Error in new instance ", e);
            throw new IllegalStateException("Error in new instance", e);
        }
    }

    private String getForeignKeyFieldName() {
        String name = parentType.getSimpleName();
        char c = name.charAt(0);
        return  Character.toLowerCase(c) + name.substring(1, name.length()  );
    }

    public void attach(RepositoryCrud crud) {
        ((HasComponents) crud.getContent()).add(this);
        crud.getGrid().addSelectionListener(this);
    }

    public void addComponent(Component component, String title) {
        tabs.addTab(title, (Component) component);
    }
}
