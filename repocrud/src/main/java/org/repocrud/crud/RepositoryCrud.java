package org.repocrud.crud;

import org.repocrud.config.SecurityUtils;
import org.repocrud.annotations.CheckBoxCollection;
import org.repocrud.annotations.EnableGroupUpdate;
import org.repocrud.components.Identifiable;
import org.repocrud.components.dialogs.ConfirmDialog;
import org.repocrud.domain.User;
import org.repocrud.history.Auditable;
import org.repocrud.repository.CommonRepository;
import org.repocrud.repository.spec.RepoSpecificationFactory;
import org.repocrud.service.ApplicationContextProvider;
import org.repocrud.text.HelpTools;
import org.repocrud.tools.PropertyTools;
import org.repocrud.ui.components.TabContainer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.crudui.crud.CrudListener;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.CrudFormConfiguration;

import javax.persistence.OneToOne;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.repocrud.text.LocalText.text;
import static java.lang.String.format;

;

@Slf4j
//@StyleSheet("frontend://context.css")
public class RepositoryCrud<T, ID> extends GridCrud<T> implements DependentView, RefreshableComponent {
    public static final String SINGLE_PREFIX = "single";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm");
    private final JpaRepository<T, ID> repository;
    private ForiegnKey<T> foriegnKey = null;

    private int page = 0;
    private int pageSize = 25;
    private Sort.Direction sortDirection = Sort.Direction.ASC;
    private String[] sortProperties = new String[]{"id"};
    private final Button buttonLeft;
    private final Label pageState;
    private final Button buttonRight;
    private final Button filter;
    private Button exportButton;
    private Checkbox multiSelect;
    private NestedTabs<T> nestedTabs = null;

    private Supplier<Long> pageCountSupplier;
    private TabContainer tabContainer = null;
    private List<RefreshListener> refreshListeners = new ArrayList<>();
    private GridContextMenu<T> contextMenu;
    private T filterInstance;

    private boolean enableAdd = true;
    private boolean enableDelete = true;
    private boolean enableUpdate = true;

    private Map<Class<?>, List<Field>> oneToOneCache = new HashMap<>();


    public RepositoryCrud(JpaRepository<T, ID> repository, RepositoryCrudFormFactory<T> crudFormFactory, ForiegnKey description) {
        this(repository, crudFormFactory);
        this.foriegnKey = description;
        pageCountSupplier = () -> (foriegnKey != null && foriegnKey.getFilter() != null) ? countByForeignKey() : countWithFilter();
    }

    private long countWithFilter() {
        if (filterInstance != null) {
            return ((JpaSpecificationExecutor) repository).count(getFilterSpecification());
        } else {
            User userDetails = (User) SecurityUtils.getUserDetails();

            if (userDetails.getCompany() != null
                    && domainType.getSuperclass() == Auditable.class) {
                return ((JpaSpecificationExecutor<T>) repository).count(RepoSpecificationFactory.getCompanyRestriction(userDetails.getCompany()));
            } else {
                return repository.count();
            }
        }


    }

    private long countByForeignKey() {
        try {
            JpaSpecificationExecutor specificationExecutor = (JpaSpecificationExecutor) repository;
            CrudFormConfiguration configuration = ((RepositoryCrudFormFactory) getCrudFormFactory()).getConfiguration(CrudOperation.READ);
            List<String> visibleProperties = configuration.getVisibleProperties();
            return specificationExecutor.count(RepoSpecificationFactory.getFilterSpecification(domainType, filterInstance, visibleProperties, foriegnKey));
        } catch (Exception e) {
            log.error("Error in count nested entities " + foriegnKey, e);
            return 0;
        }
    }


    public RepositoryCrud(JpaRepository<T, ID> repository, RepositoryCrudFormFactory<T> crudFormFactory) {
        super(crudFormFactory.getDomainType(), new RepoCrudLayout(), crudFormFactory);
        //  crudFormFactory.setUseBeanValidation(true);
        Class<T> domainType = crudFormFactory.getDomainType();
        hideOptionalField(domainType, crudFormFactory);

        this.repository = repository;
        getElement().getStyle().set("position", "absolute");
        setWidth("1000px");
        setHeight("1200px");

        pageCountSupplier = this::countWithFilter;

        this.setSavedMessage(text(RepositoryCrud.class, "saveMessage"));
        this.setDeletedMessage(text(RepositoryCrud.class, "deleteMessage"));
        this.getAddButton().getElement().setProperty("title", text(text(RepositoryCrud.class, "addButton")));

        crudLayout.addToolbarComponent(HelpTools.addHelpButton(domainType, domainType.getSimpleName()));
        buttonLeft = new Button(VaadinIcon.ARROW_LEFT.create());
        String text = "leftPage";
        setTitle(this.buttonLeft, text);
        buttonRight = new Button(VaadinIcon.ARROW_RIGHT.create());
        setTitle(buttonRight, "rightPage");
        pageState = new Label(Long.toString(page + 1));
        setTitle(pageState, "currentPageAndAll");
        pageState.getElement().getStyle().set("padding-top", "5px"); //"align-self",  "center");
        multiSelect = new Checkbox(false);
        setTitle(multiSelect, "multiSelect");
        multiSelect.getElement().getStyle().set("padding-top", "9px");//"align-self",  "center");
        multiSelect.addValueChangeListener(e -> {
            getGrid().setSelectionMode(e.getValue() ? Grid.SelectionMode.MULTI : Grid.SelectionMode.SINGLE);
            updateVisibleContextMenu(e.getValue());
            getUpdateButton().setVisible(!e.getValue() && enableUpdate);
            getDeleteButton().setVisible(!e.getValue() && enableDelete);
            getUpdateButton().setEnabled(!e.getValue() && enableUpdate);
            getDeleteButton().setEnabled(!e.getValue() && enableDelete);
        });
        filter = new Button(VaadinIcon.SEARCH.create());
        filter.addClickListener(e -> {
            filter.setEnabled(false);
            filterInstance = null;
            try {
                filterInstance = domainType.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                log.error("Error in new instance for filter", e1);
            }
            Component component = this.getCrudFormFactory()
                    .buildNewForm(CrudOperation.READ, filterInstance, false, (cancelClickEvent) -> {
                        filter.setEnabled(true);
                        filterInstance = null;
                        if (crudLayout instanceof RepoCrudLayout) {
                            ((RepoCrudLayout) crudLayout).hideFilter();
                            filterInstance = null;
                            refreshGrid();
                        }
                    }, buttonClickEvent -> {
                        refreshGrid();
                        Notification.show(text(RepositoryCrud.class, "applyFilter"));

                    });
            crudLayout.addFilterComponent(component);

        });
        setTitle(filter, "filter");

        setCrudListener(new PaginationCrudListener());


        crudLayout.addToolbarComponent(this.buttonLeft);
        crudLayout.addToolbarComponent(pageState);
        crudLayout.addToolbarComponent(buttonRight);
        crudLayout.addToolbarComponent(multiSelect);
        crudLayout.addToolbarComponent(filter);

        getGrid().addSortListener(sortEvent -> {

            sortProperties = sortEvent.getSortOrder().stream()
                    .peek(order -> sortDirection = order.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC)
                    .map(order -> order.getSorted().getKey()).toArray(String[]::new);
            super.refreshGrid();
        });


        getCrudFormFactory().setErrorListener(e -> {
            Notification.show("Ошибка " + e.getLocalizedMessage());
            log.error("Error in repocrud ", e);
        });
        //initFilters();
        initColumns();
        initNested();
        initPaginationButton();
        initContextMenu();

        addListener(ShowEvent.class, t -> refreshGrid());

    }

    public Button getFilterButton() {
        return filter;
    }

    private void setTitle(Component component, String text) {
        component.getElement().setAttribute("title", text(RepositoryCrud.class, text));
    }


    /**
     * multiSelectChBox isSingle - result
     * 0        0         0
     * 0        1         1
     * 1        0         1
     * 1        1         0
     *
     * @param isMultiSelect
     */
    private void updateVisibleContextMenu(Boolean isMultiSelect) {
        getContextMenu().getItems()
                .forEach(menuItem -> menuItem.setVisible(isMultiSelect ^ "true".equals(menuItem.getElement().getAttribute("single"))));
    }

    public NestedTabs<T> getNestedTabs() {
        return nestedTabs;
    }

    private void initContextMenu() {

        contextMenu = new GridContextMenu<>(getGrid());

        GridMenuItem updateItem = addContextMenuItem(text(RepositoryCrud.class, "Update"), VaadinIcon.PENCIL.create(), t -> {
            if (enableUpdate) {
                updateButtonClicked();
            } else {
                Notification.show(text(RepositoryCrud.class, "updateIsDisabled"));
            }

        }, "update");

        GridMenuItem deleteItem = addContextMenuItem(
                text(RepositoryCrud.class, "Delete"), VaadinIcon.TRASH.create(), t -> {
//                    if (getDeleteButton().isVisible()) {
                    deleteButtonClicked();
//                    } else {
//                        Notification.show(text(RepositoryCrud.class, "deleteIsDisabled"));
//                    }
                }, "delete");

        List<String> groupVisibleProperties = Stream.of(domainType.getDeclaredFields())
                .filter(field -> Stream.of(field.getAnnotations()).anyMatch(a -> a instanceof EnableGroupUpdate))
                .map(Field::getName)
                .collect(Collectors.toList());
        if (groupVisibleProperties.size() > 0) {
            addGroupUpdateContextMenu(groupVisibleProperties);
        }
        addGroupDeleteContextMenu();
    }

    private void addGroupDeleteContextMenu() {
        GridMenuItem groupDelete = addGroupContextMenuItem(text(RepositoryCrud.class, "DeleteAll"), VaadinIcon.TRASH.create(), "delete",
                ts -> {
                    Label labelWithCount = new Label(text(RepositoryCrud.class, "deleteTheNextObjects", ts.size()));
                    ConfirmDialog.ConfirmAction confirmAction = () -> saveDelete(ts);
                    ConfirmDialog dialog = new ConfirmDialog(RepositoryCrud.class, "DeleteAll", "delete", "cancel", 300,
                            confirmAction,
                            labelWithCount);
                    dialog.open();
                });

    }

    private void addGroupUpdateContextMenu(List<String> groupVisibleProperties) {
        GridMenuItem updateItem = addGroupContextMenuItem(text(RepositoryCrud.class, "groupUpdate"), VaadinIcon.PENCIL.create(), "update",
                ts -> {
                    CrudFormConfiguration configuration = ((RepositoryCrudFormFactory) getCrudFormFactory()).getConfiguration(CrudOperation.UPDATE);
                    List<String> visibleProperties = configuration.getVisibleProperties();

                    try {
                        T groupInstance = domainType.newInstance();
                        configuration.setVisibleProperties(groupVisibleProperties);
                        this.showForm(CrudOperation.UPDATE, groupInstance, false, this.savedMessage, buttonClickEvent -> {
                                    groupVisibleProperties.stream()
                                            .map(property -> BeanUtils.getPropertyDescriptor(domainType, property))
                                            .filter(Objects::nonNull)
                                            .map(propertyDescriptor ->
                                                    Pair.of(propertyDescriptor, PropertyTools.read(groupInstance, propertyDescriptor.getReadMethod()))

                                            ).filter(p -> p.getValue() != null)
                                            .forEach(p -> ts.forEach(t -> PropertyTools.write(t, p.getLeft().getWriteMethod(), p.getValue())));

                                    try {
                                        log.info("Group updating {} for {}", groupInstance, ts.size());
                                        repository.saveAll(ts);
                                        refreshGrid();
                                        Notification.show(text(RepositoryCrud.class, "groupUpdated", ts.size()));
                                    } catch (ConstraintViolationException exception) {
                                        showConstraintException(exception);
                                        log.error("Error in group update " + groupInstance, exception);
                                    }
                                }


                        );
                    } catch (InstantiationException | IllegalAccessException e) {
                        log.error("Error in create group instace for " + domainType, e);
                        Notification.show(text("error", e.getMessage()));
                    } finally {
                        configuration.setVisibleProperties(visibleProperties);
                    }

                });
    }

    private boolean saveDelete(Set<T> ts) {
        try {
            log.info("try to delete {}", ts);
            ts.forEach(t -> deleteOperation.perform(t));
//            repository.deleteAll(ts);
            log.info("Success delete {}", ts);
            refreshGrid();
            Notification.show(text(RepositoryCrud.class, "successDelete"));
            return true;
        } catch (Exception e) {
            String message = getConstraintMessage(e);
            log.info("Exception in delete all {}", message);
            Notification.show(message, 2000, Notification.Position.MIDDLE);
            return false;
        }
    }

    private String getConstraintMessage(Exception e) {
        boolean constraintMessage = e instanceof DataIntegrityViolationException
                && e.getCause() != null && e.getCause() instanceof org.hibernate.exception.ConstraintViolationException
                && e.getCause().getCause() != null && e.getCause().getCause() instanceof SQLException;
        return constraintMessage ? e.getCause().getCause().getMessage() : e.getMessage();
    }

    public GridMenuItem addContextMenuItem(String text, Icon icon, String actionName, Consumer<T> action, Function<T, Boolean> enableFunction) {
        int count = contextMenu.getItems().size();
        GridMenuItem menuItem = contextMenu.addItem(
                new HorizontalLayout(icon, new Label(text))
                , e -> {
                    if (e.getItem().isPresent()) {
                        e.getItem().ifPresent(t -> {
                            getGrid().select(t);
                            action.accept(t);
                        });
                    } else {
                        Notification.show(text(RepositoryCrud.class, "nothingSelect"));
                    }
                });
        menuItem.setId(SINGLE_PREFIX + count);
        menuItem.getElement().setAttribute("single", "true");
        menuItem.getElement().setAttribute("action", actionName);
        if (enableFunction != null) {
            grid.addSelectionListener((SelectionListener<Grid<T>, T>) selectionEvent -> {
                if (selectionEvent.getAllSelectedItems().size() == 1) {
                    T next = selectionEvent.getAllSelectedItems().iterator().next();
                    Boolean apply = enableFunction.apply(next);
                    menuItem.setEnabled(apply);
                }
            });
        }
        return menuItem;
    }

    public GridMenuItem addContextMenuItem(String text, Icon icon, Consumer<T> action, String actionName) {
        return addContextMenuItem(text, icon, actionName, action, null);
    }

    public GridMenuItem addGroupContextMenuItem(String text, Icon icon, String actionName, Consumer<Set<T>> action) {
        int count = contextMenu.getItems().size();
        GridMenuItem menuItem = contextMenu.addItem(
                new HorizontalLayout(icon, new Label(text))
                , e -> {
                    Set<T> selectedItems = getGrid().getSelectedItems();
                    if (selectedItems.size() > 0) {
                        action.accept(selectedItems);
                    } else {
                        Notification.show(text(RepositoryCrud.class, "nothingSelect"));
                    }
                });
        menuItem.setId("multiple" + count);
        menuItem.getElement().setAttribute("single", "false");
        menuItem.getElement().setAttribute("action", actionName);
        return menuItem;
    }

    public void hideOptionalField(Class<T> domainType, RepositoryCrudFormFactory<T> crudFormFactory) {
        if (domainType.getSuperclass() == Auditable.class) {
            return;
        }
        try {
            Field id = domainType.getDeclaredField("id");
            if (Number.class.isAssignableFrom(id.getType())) {
                crudFormFactory.hideVisibleProperties("id");
                log.info("Hide general id for entity [{}]", domainType);
            } else {
                log.info("Entity [{}] contains isn't general id type [{}]", domainType, id.getType());
            }
        } catch (NoSuchFieldException e) {
            log.warn("Entity [{}] doesn't contains 'id'", domainType);
        }
    }


    public RepositoryCrud(Class<T> domainType, JpaRepository<T, ID> repository) {
        this(repository, new RepositoryCrudFormFactory<T>(domainType));
    }


    public void setSortProperties(String... sortProperties) {
        this.sortProperties = sortProperties;
    }

    public void setSortDirection(Sort.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    private void initNested() {
        CrudFormConfiguration configuration = ((RepositoryCrudFormFactory) getCrudFormFactory()).getConfiguration(CrudOperation.READ);
        List<String> visibleProperties = configuration.getVisibleProperties();


        nestedTabs = new NestedTabs<T>(this.domainType, visibleProperties);
        //nestedTabs.attach(this);
        getContent().add(nestedTabs);
        getGrid().addSelectionListener(nestedTabs);

    }

    public void addDependentView(Component component, Consumer<T> onSelect, String title) {
        getGrid().addSelectionListener(event -> {
            Set<T> selectedItems = getGrid().getSelectedItems();
            if (selectedItems.size() == 1) {
                onSelect.accept(selectedItems.iterator().next());
            } else {
                onSelect.accept(null);
            }
        });
        nestedTabs.addComponent(component, title);
    }

    private void initColumns() {
        getGrid().getColumns().stream().forEach(tColumn -> getGrid().removeColumn(tColumn));
        if (getCrudFormFactory() instanceof RepositoryCrudFormFactory) {
            CrudFormConfiguration configuration = ((RepositoryCrudFormFactory) getCrudFormFactory()).getConfiguration(CrudOperation.READ);
            List<String> visibleProperties = configuration.getVisibleProperties();
            Stream.of(domainType.getDeclaredFields())
                    .filter(field -> !(Number.class.isAssignableFrom(field.getType()) && field.getType().getName().equals("id")))
                    .filter(field -> !Collection.class.isAssignableFrom(field.getType())
                            || (Stream.of(field.getAnnotations()).anyMatch(annotation -> annotation instanceof CheckBoxCollection) && Collection.class.isAssignableFrom(field.getType())))
                    .filter(field -> visibleProperties.contains(field.getName()))
                    .forEach(this::addColumn);
        } else {
            Stream.of(domainType.getDeclaredFields()).forEach(this::addColumn);
        }
    }

    private void addColumn(Field field) {
        final Grid.Column<T> tColumn;
        Optional<Annotation> first;
        if (field.getType() == Boolean.class || field.getType().getName().equals("boolean")) {
            tColumn = getGrid().addComponentColumn(t -> getCheckbox(field, t));
        } else if ((first = Stream.of(field.getAnnotations()).filter(annotation -> annotation instanceof CheckBoxCollection).findFirst()).isPresent()) {
            CheckBoxCollection checkBoxCollection = (CheckBoxCollection) first.get();
            CommonRepository repository = (CommonRepository) ApplicationContextProvider.getRepository(checkBoxCollection.type());
            tColumn = getGrid().addComponentColumn(t -> new Label(getText(repository.lazyLoadEnumList((Collection) getValue(field, t), checkBoxCollection))));
        } else if (field.getType() == ZonedDateTime.class) {
            tColumn = getGrid().addComponentColumn(t -> getLabel(field, t, o -> DATE_TIME_FORMATTER.format((TemporalAccessor) o)));
        } else {
            tColumn = getGrid().addComponentColumn(t -> getLabel(field, t));

        }


        tColumn.setKey(field.getName());
        tColumn.setHeader(text(domainType, field.getName()));
        tColumn.setSortable(true);
    }

    private String getText(Collection collection) {
        String collect = (String) collection.stream().map(o -> text(o.getClass(), o.toString())).collect(Collectors.joining(","));
        return collect;
    }

    private Component getLabel(Field field, T t) {
        Object value = getValue(field, t);

        return value != null ? new Label(value.toString()) : new Label();
    }

    private Component getLabel(Field field, T t, Function<Object, String> formatter) {
        Object value = getValue(field, t);
        return value != null ? new Label(formatter.apply(value)) : new Label();
    }

    private Checkbox getCheckbox(Field field, T t) {

        Object value = getValue(field, t);

        Checkbox checkbox = new Checkbox();
        checkbox.setReadOnly(true);
        if (value != null) {
            checkbox.setValue((Boolean) value);
        }
        return checkbox;
    }

    private Object getValue(Field field, T t) {
        Object value = null;
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            value = field.get(t);
        } catch (IllegalAccessException e) {
            log.error("Error in value provider");
            throw new IllegalStateException(e);
        }
        return value;
    }

    protected void findAllButtonClicked() {
        GridSelectionModel<T> selectionModel = this.grid.getSelectionModel();
        if (selectionModel instanceof GridSingleSelectionModel) {
            this.grid.asSingleSelect().clear();
        } else {
            this.grid.asMultiSelect().clear();
        }
        this.refreshGrid();
        Notification.show(String.format(this.rowCountCaption, this.grid.getDataProvider().size(new Query())));
    }

    @Override
    public void refreshGrid() {
        if (isVisible() && AbstractCrudContainer.parentTabVisible(getElement().getParent())) {
            log.info("Refresh CRUD {}", domainType.getSimpleName());
            Set<T> selectedItems = null;
            GridSelectionModel<T> selectionModel = getGrid().getSelectionModel();
            if (multiSelect.getValue()) {
                selectedItems = getGrid().getSelectedItems();
                getGrid().setSelectionMode(Grid.SelectionMode.SINGLE);
            }
            super.refreshGrid();
            long count = pageCountSupplier.get();
            page = page * pageSize > count ? (int) count / pageSize : page;
            buttonRight.setEnabled(page < count / pageSize);
            buttonLeft.setEnabled(page > 0);
            pageState.setText(format("%d/%d", page + 1, count / pageSize + 1));
            refreshListeners.forEach(RefreshListener::refresh);
            updateVisibleContextMenu(multiSelect.getValue());
            if (multiSelect.getValue() && selectedItems != null) {
                getGrid().setSelectionMode(Grid.SelectionMode.MULTI);
                selectedItems.forEach(t -> grid.select(t));
            }
        }

    }

    private void initPaginationButton() {
        buttonLeft.addClickListener(buttonClickEvent -> {
            page--;
            refreshGrid();
        });

        buttonRight.addClickListener(buttonClickEvent -> {
            page++;
            refreshGrid();
        });
    }

    void setForiegnKey(ForiegnKey description) {
        this.foriegnKey = description;
    }


    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageCountSupplier(Supplier<Long> pageCountSupplier) {
        this.pageCountSupplier = pageCountSupplier;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void addCustomBooleanColumn(String titleKey, ValueProvider<T, Boolean> valueProvider) {
        Grid.Column<T> tColumn = getGrid().addComponentColumn(t -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(valueProvider.apply(t));
            checkbox.setReadOnly(true);
            return checkbox;

        });
        tColumn.setHeader(text(domainType, titleKey));
        tColumn.setWidth("50px");
    }

    public Grid.Column<T> addCustomColumn(String titleKey, ValueProvider<T, String> valueProvider) {
        Grid.Column<T> tColumn = getGrid().addComponentColumn(t -> new Label(valueProvider.apply(t)));
        tColumn.setHeader(text(domainType, titleKey));
        return tColumn;
    }

    public <V extends Component> Grid.Column<T> addComponentColumn(String titleKey,
                                                                   ValueProvider<T, V> componentProvider) {
        Grid.Column<T> tColumn = getGrid().addComponentColumn(componentProvider);
        tColumn.setHeader(text(domainType, titleKey));
        return tColumn;
    }

    public void addExportButton() {
        exportButton = new Button(text(RepositoryCrud.class, "export"));
        crudLayout.addToolbarComponent(exportButton);
        throw new NotImplementedException("Not implemented export in Repository CRUD level");
    }

    protected void addButtonClicked() {
        try {
            T domainObject = this.domainType.newInstance();
            RepeatAction repeatAction = new RepeatAction();
            ComponentEventListener<ClickEvent<Button>> eventListener = (event) -> {
                try {
                    T addedObject;
                    try {
                        addedObject = this.addOperation.perform(domainObject);
                    } catch (TransactionSystemException te) {
                        ConstraintViolationException ce = findConstraintException(te, 5);
                        showConstraintException(ce);
                        if (repeatAction.action != null) {
                            repeatAction.action.act();
                        }
                        return;

                    } catch (ConstraintViolationException e) {
                        showConstraintException(e);
                        if (repeatAction.action != null) {
                            repeatAction.action.act();
                        }
                        return;
                    }
                    this.refreshGrid();
                    if (this.getGrid().getSelectionModel() instanceof GridSingleSelectionModel) {
                        this.grid.asSingleSelect().setValue(addedObject);
                    }
                } catch (IllegalArgumentException var4) {
                    throw var4;
                } catch (CrudOperationException var5) {
                    this.refreshGrid();
                } catch (Exception var6) {
                    this.refreshGrid();
                    throw new IllegalStateException(var6);
                }

            };
            repeatAction.action = () -> this.showForm(CrudOperation.ADD, domainObject, false, this.savedMessage, eventListener);
            repeatAction.action.act();
        } catch (IllegalAccessException | InstantiationException var2) {
            var2.printStackTrace();
        }

    }

    protected void updateButtonClicked() {
        T domainObject = this.grid.asSingleSelect().getValue();
        RepeatAction repeatAction = new RepeatAction();
        ComponentEventListener<ClickEvent<Button>> event = (e) -> {
            try {
                T updatedObject;
                try {
                    updatedObject = this.updateOperation.perform(domainObject);
                } catch (TransactionSystemException te) {
                    ConstraintViolationException ce = findConstraintException(te, 5);
                    showConstraintException(ce);
                    if (repeatAction.action != null) {
                        repeatAction.action.act();
                    }
                    return;

                } catch (ConstraintViolationException exception) {
                    showConstraintException(exception);
                    if (repeatAction.action != null) {
                        repeatAction.action.act();
                    }
                    return;
                }
                this.grid.asSingleSelect().clear();
                this.refreshGrid();
                this.grid.asSingleSelect().setValue(updatedObject);
            } catch (IllegalArgumentException var4) {
                throw var4;
            } catch (CrudOperationException var5) {
                this.refreshGrid();
            } catch (Exception var6) {
                this.refreshGrid();
                throw new IllegalStateException(var6);
            }

        };
        repeatAction.action = () -> this.showForm(CrudOperation.UPDATE, domainObject, false, this.savedMessage, event);
        repeatAction.action.act();
    }

    private ConstraintViolationException findConstraintException(Exception te, int level) throws Exception {
        if (level == 0) {
            throw te;
        }
        if (te.getCause() instanceof ConstraintViolationException) {
            return (ConstraintViolationException) te.getCause();
        } else {
            return findConstraintException((Exception) te.getCause(), level - 1);
        }
    }


    public void addRefreshListener(RefreshListener listener) {
        refreshListeners.add(listener);
    }

    public GridContextMenu<T> getContextMenu() {
        return contextMenu;
    }

    public void showConstraintException(ConstraintViolationException e) {
        Map<String, List<ConstraintViolation<?>>> map = e.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(ConstraintViolation::getMessage, Collectors.toList()));

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<ConstraintViolation<?>>> entry : map.entrySet()) {
            builder.append(entry.getKey());
            builder.append(entry.getValue().stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(Path::toString)
                    .map(s -> text(domainType, s))
                    .collect(Collectors.joining(",", ": ", ";")));
        }
        Notification.show(builder.toString(), 3000, Notification.Position.MIDDLE);
    }

    private class PaginationCrudListener implements CrudListener<T> {

        @Override
        public Collection<T> findAll() {
            final PageRequest pageRequest = getPageRequest();

            if (foriegnKey != null && foriegnKey.getFilter() != null) {
                try {
                    CrudFormConfiguration configuration = ((RepositoryCrudFormFactory) getCrudFormFactory()).getConfiguration(CrudOperation.READ);
                    List<String> visibleProperties = configuration.getVisibleProperties();
                    JpaSpecificationExecutor repository = (JpaSpecificationExecutor) RepositoryCrud.this.repository;
                    return repository.findAll(RepoSpecificationFactory.getFilterSpecification(domainType, filterInstance, visibleProperties, foriegnKey), pageRequest).getContent();

                } catch (Exception e) {
                    log.error("Error in load nested entities " + foriegnKey, e);
                    return Collections.emptyList();
                }
            }

            User userDetails = (User) SecurityUtils.getUserDetails();
            if (filterInstance != null) {
                return ((JpaSpecificationExecutor<T>) repository).findAll(getFilterSpecification(), pageRequest).getContent();
            } else if (userDetails.getCompany() != null
                    && domainType.getSuperclass() == Auditable.class) {
                return ((JpaSpecificationExecutor<T>) repository).findAll(RepoSpecificationFactory.getCompanyRestriction(userDetails.getCompany()), pageRequest).getContent();
            } else {
                return repository.findAll(pageRequest).getContent();
            }

        }

        @Override
        public T add(T t) {
            if (foriegnKey != null) {
                foriegnKey.initForeignKey(t);
            }
            return saveAndFlush(t);
        }


        @Override
        public T update(T t) {
            return saveAndFlush(t);
        }

        @Override
        @Transactional
        public void delete(T t) {
            try {
                deleteWithOneToOneUpdate(t);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                String message = getConstraintMessage(e);
                throw new RuntimeException(message);
            }
        }
    }

    private void deleteWithOneToOneUpdate(T t) {
        List<Field> fields = getOneToOneFields(domainType, true);
        fields
                .forEach(field -> {
                            updateUpOneToOne(t, field, true);
                        }
                );
        repository.delete(t);

    }

    private T saveAndFlush(T t) {
        T result = repository.saveAndFlush(t);
        List<Field> fields = getOneToOneFields(domainType, true);
        fields.forEach(field -> updateUpOneToOne(result, field, false));
        return result;
    }

    private void updateUpOneToOne(T t, Field field, boolean removeForeignReference) {
        try {
            log.info("Update {} one to one reference {}", t, field.getName());
            String foreignKey = getOneToOneFields(field.getType(), false).stream().filter(f -> f.getType() == domainType).findFirst().get().getName();
            Object linkObject = BeanUtils.getPropertyDescriptor(domainType, field.getName()).getReadMethod().invoke(t);

            JpaRepository repository = ApplicationContextProvider.getRepository(field.getType());
            Optional one = ((JpaSpecificationExecutor) repository).findOne((e, cq, cb) -> cb.equal(e.get(foreignKey), ((Identifiable) t).getId()));
            if (one.isPresent()) {
                Object prevLinkObject = one.get();
                boolean equals = prevLinkObject.equals(linkObject);
                if (!equals || removeForeignReference) {
                    BeanUtils.getPropertyDescriptor(prevLinkObject.getClass(), foreignKey)
                            .getWriteMethod().invoke(prevLinkObject, new Object[]{ null });
                    repository.saveAndFlush(prevLinkObject);
                }
                if (linkObject != null && !equals && !removeForeignReference) {
                    BeanUtils.getPropertyDescriptor(linkObject.getClass(), foreignKey).getWriteMethod().invoke(linkObject, t);
                    repository.saveAndFlush(linkObject);
                }
            } else if (linkObject != null && !removeForeignReference) {
                BeanUtils.getPropertyDescriptor(linkObject.getClass(), foreignKey).getWriteMethod().invoke(linkObject, t);
                repository.saveAndFlush(linkObject);
            }

        } catch (Exception e) {
            log.error("Error in update one to one relation " + t, e);
        }
    }

    private List<Field> getOneToOneFields(Class<?> keyType, boolean mappedBy) {
        return oneToOneCache.computeIfAbsent(keyType, key -> Stream.of(key.getDeclaredFields())
                .filter(field -> Stream.of(field.getAnnotations())
                        .anyMatch(a -> a instanceof OneToOne && (!mappedBy || ((OneToOne) a).mappedBy().length() > 0)))
                //.anyMatch(a -> a instanceof OneToOne && ((OneToOne) a).mappedBy().length() > 0))
                .collect(Collectors.toList()));
    }

    private Specification<T> getFilterSpecification() {
        CrudFormConfiguration configuration = ((RepositoryCrudFormFactory) getCrudFormFactory()).getConfiguration(CrudOperation.READ);
        List<String> visibleProperties = configuration.getVisibleProperties();
        return RepoSpecificationFactory.getFilterSpecification(domainType, filterInstance, visibleProperties);
    }

    public PageRequest getPageRequest() {
        final PageRequest pageRequest;
        if (sortProperties != null && sortProperties.length == 1) {
            pageRequest = PageRequest.of(page, pageSize, sortDirection, sortProperties);
        } else {
            pageRequest = PageRequest.of(page, pageSize);
        }
        return pageRequest;
    }


    @Override
    public void refresh() {
        refreshGrid();
    }


    @Override
    public void setAddOperationVisible(boolean visible) {
        getAddButton().setEnabled(visible);
        getAddButton().setVisible(visible);
        this.enableAdd = visible;

    }

    @Override
    public void setUpdateOperationVisible(boolean visible) {
        getUpdateButton().setEnabled(visible);
        getUpdateButton().setVisible(visible);
        this.enableUpdate = visible;
        hideMenuItem("update");
    }

    @Override
    public void setDeleteOperationVisible(boolean visible) {
        getDeleteButton().setEnabled(visible);
        getDeleteButton().setVisible(visible);
        this.enableDelete = visible;
        hideMenuItem("delete");
    }

    private void hideMenuItem(String actionName) {
        contextMenu.getItems().stream()
                .filter(menuItem -> {

                    return actionName.equals(menuItem.getElement().getAttribute("action"));
                })
                .forEach(menuItem -> {
                    menuItem.setVisible(false);
                    menuItem.setEnabled(false);
                });
    }

    public interface RefreshListener {
        public void refresh();
    }

    static class RepeatAction {
        Action action;
    }

    @FunctionalInterface
    private interface Action {
        void act();
    }

    public static class ShowEvent extends ComponentEvent<RepositoryCrud> {
        public ShowEvent(RepositoryCrud source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
