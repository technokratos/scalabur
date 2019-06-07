package org.repocrud.components;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * @author Denis B. Kulikov<br/>
 * date: 04.10.2018:0:01<br/>
 */
@Slf4j
public class DateField extends HorizontalLayout
        implements HasValueAndElement<ComponentValueChangeEvent<DateField, LocalDate>, LocalDate> {

    private final TextField timeField;
    private LocalDate value;
    private final DatePicker datePicker;


    public DateField() {
        this((String) null);
    }

    public DateField(String label) {
        timeField = label != null ? new TextField(label) : new TextField();
        datePicker = new DatePicker();
        add(datePicker);
    }

    public DateField(LocalDate localTime, String label) {
        this(label);
        setValue(localTime);

    }

//    public void setRemoveAction(RemoveAction removeAction) {
//        this.removeAction = removeAction;
//    }

    public DateField(LocalDate localTime) {
        this(localTime, null);

    }

    @Override
    public void setValue(LocalDate value) {
        this.value = value;
        this.datePicker.setValue(value);

    }

    @Override
    public LocalDate getValue() {
        return value;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<DateField, LocalDate>> listener) {

        ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> changeListener = event -> {
            ComponentValueChangeEvent<DateField, LocalDate> dateEvent = new
                    ComponentValueChangeEvent<>(DateField.this,
                    DateField.this,
                    value, event.isFromClient());
            listener.valueChanged(dateEvent);
        };
        Registration registrationPicker = datePicker.addValueChangeListener(changeListener);

        ValueChangeListener<ComponentValueChangeEvent<TextField, String>> changeListener2 = event -> {

            LocalDate oldValue = value;
            ComponentValueChangeEvent<DateField, LocalDate> innerEvent =
                    new ComponentValueChangeEvent<>(
                            DateField.this, DateField.this,
                            oldValue, event.isFromClient());
            listener.valueChanged(innerEvent);
        };
        Registration registrationTextField = timeField.addValueChangeListener(changeListener2);
        return (Registration) () -> {
            registrationPicker.remove();
            registrationTextField.remove();
        };
    }


    @Override
    public void setReadOnly(boolean readOnly) {
        timeField.setReadOnly(true);
        datePicker.setReadOnly(true);
    }

    @Override
    public boolean isReadOnly() {
        return timeField.isReadOnly() && datePicker.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        timeField.setRequiredIndicatorVisible(requiredIndicatorVisible);
        datePicker.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return timeField.isRequiredIndicatorVisible() && datePicker.isRequiredIndicatorVisible();
    }
}
