package org.repocrud.crud;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.form.CrudFormConfiguration;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.repocrud.text.LocalText.text;

public class RepositoryCrudFormFactory<T> extends DefaultCrudFormFactory<T> {


    private List<Function<T, Component>> customComponents = new ArrayList<>();

    private final FormLayout.ResponsiveStep[] responsiveSteps;
    public RepositoryCrudFormFactory(Class<T> domainType) {
        this(domainType, null);
    }

    public Binder<T> getBinder( ) {
        return binder;
    }

    public Class<T> getDomainType() {
        return domainType;
    }

    public RepositoryCrudFormFactory(Class<T> domainType, FormLayout.ResponsiveStep... responsiveSteps) {
        super(domainType, responsiveSteps);
        if (responsiveSteps != null) {
            this.responsiveSteps = responsiveSteps;
        } else {
            this.responsiveSteps = new FormLayout.ResponsiveStep[]{new FormLayout.ResponsiveStep("0em", 1), new FormLayout.ResponsiveStep("25em", 2)};
        }
        hideVisibleProperties("id");
        Stream.of(domainType.getDeclaredFields()).forEach(field -> {
            UpdateFieldProvider provider = new UpdateFieldProvider(field.getType(), field);
            setFieldProvider(CrudOperation.UPDATE, field.getName(), provider);
            setFieldProvider(CrudOperation.ADD, field.getName(), provider);
            setFieldProvider(CrudOperation.READ, field.getName(), provider);
        });
    }

    @Override
    public CrudFormConfiguration getConfiguration(CrudOperation operation) {
        return super.getConfiguration(operation);
    }

    public void hideVisibleProperties(String... properties) {
        Stream.of(CrudOperation.values()).forEach(crudOperation -> hideVisibleProperties(crudOperation, properties));
    }
    public void hideVisibleProperties(CrudOperation operation, String... properties) {
        CrudFormConfiguration configuration =  getConfiguration(operation);
        List<String> visibleProperties = configuration.getVisibleProperties();
        Set<String> set = Stream.of(properties).collect(Collectors.toSet());
        List<String> collect = visibleProperties.stream().filter(s -> !set.contains(s)).collect(Collectors.toList());
        configuration.setVisibleProperties(collect);
    }

    @Override
    public Component buildNewForm(CrudOperation operation, T domainObject, boolean readOnly, ComponentEventListener<ClickEvent<Button>> cancelButtonClickListener, ComponentEventListener<ClickEvent<Button>> operationButtonClickListener) {
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setResponsiveSteps(this.responsiveSteps);
        List<String> visibleProperties = getConfiguration(operation).getVisibleProperties();
        List<String> captions = visibleProperties.stream().map(key -> text(domainType, key)).collect(Collectors.toList());
        getConfiguration(operation).setFieldCaptions(captions);
        List<HasValueAndElement> fields = this.buildFields(operation, domainObject, readOnly);
        fields.stream().forEach((field) -> {
            Element var10000 = (Element)formLayout.getElement().appendChild(new Element[]{field.getElement()});
        });
        customComponents.forEach(function -> {
            Element var10000 = (Element)formLayout.getElement().appendChild(function.apply(domainObject).getElement());
        });
        Component footerLayout = this.buildFooter(operation, domainObject, cancelButtonClickListener, operationButtonClickListener);
        VerticalLayout mainLayout = new VerticalLayout(new Component[]{formLayout, footerLayout});
        mainLayout.setFlexGrow(1.0D, new HasElement[]{formLayout});
        mainLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, new Component[]{footerLayout});
        mainLayout.setMargin(false);
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        return mainLayout;
    }

    public void addCustomComponent(Function<T, Component> function) {
        customComponents.add(function);
    }
}
