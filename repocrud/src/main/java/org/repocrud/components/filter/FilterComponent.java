package org.repocrud.components.filter;

import org.repocrud.components.DateTimeField;
import org.repocrud.domain.Filter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.repocrud.domain.Filter.Operation.*;
import static java.lang.String.format;

/**
 * @author Denis B. Kulikov<br/>
 * date: 22.11.2018:9:19<br/>
 */
@Slf4j
public class FilterComponent extends HorizontalLayout {
    private FieldWrapper field;

    private final Filter filter;


    FilterComponent(Filter filter, List<FieldWrapper> fieldWrappers) {
        this.filter = filter;

        Optional<FieldWrapper> first = fieldWrappers.stream().filter(fieldWrapper -> fieldWrapper.toString().equals(filter.getField())).findFirst();
        if (!first.isPresent()) {
            return;
        }
        field = first.get();

        Class type = field.getType();

        ComboBox<FieldWrapper> fieldsBox = new ComboBox<>();
        fieldsBox.setItems(fieldWrappers);
        fieldsBox.setValue(field);

        final HasValueAndElement component;

        ComboBox<Filter.Operation> operationBox = new ComboBox<>("", values());
        operationBox.setValue(filter.getOperation());

        Div div = new Div();
        HasValueAndElement initField = getField(this.field);
        div.add((Component) initField);

        setValue(filter, type, initField);

        fieldsBox.addValueChangeListener(event -> {
            HasValueAndElement valueComponent = getField(event.getValue());
            div.removeAll();
            div.add((Component) valueComponent);
            valueComponent.addValueChangeListener(valueEvent-> {
                filter.setValue(valueEvent.getValue().toString());
                //todo save
            });
        });

        add(fieldsBox, operationBox, (Component) initField);

        //todo add
    }


    public Filter getFilter() {
        return filter;
    }

    private void setValue(Filter filter, Class type, HasValueAndElement initField) {
        FieldType fieldType = FieldType.typeMap.get(type);
        if (fieldType != null) {
            initField.setValue(fieldType.setConverter.apply(filter.getValue()));
        } else {
            log.error("Not found FieldType for {}, {}", type, filter);
        }
    }

    private HasValueAndElement getField(FieldWrapper value) {
        FieldType fieldType = FieldType.typeMap.get(value.getType());
        Class<?> type = this.field.getType();
        if (fieldType != null) {
            return fieldType.supplier.get();
        } else {
            log.error("Not found FieldType for {}, {}", type, filter);
        }
        throw new IllegalStateException(format("Not supported type %s, property %s",  value.getType(), value.toString()) );


    }

    enum FieldType {
        Zoned(DateTimeField::new, LocalDateTime::parse, ZonedDateTime.class, AFTER, BEFORE, EQUAL),
        Local(DateTimeField::new, LocalDateTime::parse, LocalDateTime.class, AFTER, BEFORE, EQUAL),
        Local2(TextField::new, LocalTime::parse, LocalTime.class, AFTER, BEFORE, EQUAL),

        IntField(TextField::new, Integer::parseInt, Integer.class, AFTER, BEFORE, EQUAL),
        LongField(TextField::new, Long::parseLong, Long.class, AFTER, BEFORE, EQUAL),
        DoubleField(TextField::new, Double::parseDouble, Double.class, AFTER, BEFORE, EQUAL),

        BooleanField(Checkbox::new, Boolean::parseBoolean, Boolean.class, EQUAL),
        StringField(TextField::new, (value) -> value,String.class, EQUAL, LIKE);


        private final Class type;

        private final Supplier<HasValueAndElement> supplier;
        private final Function<String, Object> setConverter;
        private final Filter.Operation[] operations;

        static Map<Class, FieldType> typeMap = new HashMap<>();
        static {
            typeMap = Stream.of(FieldType.values()).collect(Collectors.toMap(o -> o.type, o -> o));
        }

        public FieldType byType(Class type) {
            return typeMap.get(type);
        }

        FieldType(Supplier<HasValueAndElement> supplier, Function<String, Object> setConverter, Class type, Filter.Operation... operations) {
            this.type = type;
            this.supplier = supplier;
            this.setConverter = setConverter;
            this.operations = operations;
        }
    }
}
