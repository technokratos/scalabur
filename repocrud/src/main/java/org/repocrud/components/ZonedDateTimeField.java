package org.repocrud.components;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.*;

/**
 * @author Denis B. Kulikov<br/>
 * date: 04.10.2018:0:01<br/>
 */
@Slf4j
public class ZonedDateTimeField extends HorizontalLayout
        implements HasValueAndElement<ComponentValueChangeEvent<ZonedDateTimeField, ZonedDateTime>, ZonedDateTime> {

    private final TextField timeField;
    private ZonedDateTime value;
    private final DatePicker datePicker;


    public ZonedDateTimeField() {
        this((String) null);
    }

    public ZonedDateTimeField(String label) {
        timeField = label != null ? new TextField(label) : new TextField();
        datePicker = new DatePicker();
        timeField.addValueChangeListener(event -> {
            if (StringUtils.isEmpty(timeField.getValue())) {
                value = null;
            } else {
                try {
                    value = LocalDateTime.parse(timeField.getValue()).atZone(ZoneId.systemDefault());
                } catch (Exception e) {
                    value = null;
                    timeField.setValue(null);
                }
            }
        });
        timeField.addValueChangeListener(event -> {
            if (StringUtils.isEmpty(timeField.getValue())) {
                this.value = null;
            } else {
                try {
                    LocalTime localTime = LocalTime.parse(timeField.getValue());
                    this.value = LocalDateTime.of(datePicker.getValue(), localTime).atZone(ZoneId.systemDefault());
                } catch (Exception e) {
                    log.error("Parsing data is wrong e", e);
                    timeField.setValue(event.getOldValue());
                }
            }
        });

        add(datePicker, timeField);
    }

    public ZonedDateTimeField(ZonedDateTime localTime, String label) {
        this(label);
        setValue(localTime);

    }

//    public void setRemoveAction(RemoveAction removeAction) {
//        this.removeAction = removeAction;
//    }

    public ZonedDateTimeField(ZonedDateTime localTime) {
        this(localTime, null);

    }

    @Override
    public void setValue(ZonedDateTime value) {
        this.value = value;
        timeField.setValue(value.toLocalTime().toString());
        this.datePicker.setValue(value.toLocalDate());

    }

    @Override
    public ZonedDateTime getValue() {
        return value;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<ZonedDateTimeField, ZonedDateTime>> listener) {

        ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> changeListener = event -> {
            LocalDate oldValue = event.getOldValue();
            ComponentValueChangeEvent<ZonedDateTimeField, ZonedDateTime> dateEvent = new
                    ComponentValueChangeEvent<>(ZonedDateTimeField.this,
                    ZonedDateTimeField.this,
                    LocalDateTime.of(oldValue, value.toLocalTime()).atZone(ZoneId.systemDefault()), event.isFromClient());
        };
        Registration registrationPicker = datePicker.addValueChangeListener(changeListener);

        ValueChangeListener<ComponentValueChangeEvent<TextField, String>> changeListener2 = event -> {

            LocalDateTime oldValue = LocalDateTime.of(value.toLocalDate(), LocalTime.parse(event.getOldValue()));
            ComponentValueChangeEvent<ZonedDateTimeField, ZonedDateTime> innerEvent =
                    new ComponentValueChangeEvent<>(
                            ZonedDateTimeField.this, ZonedDateTimeField.this,
                            oldValue.atZone(ZoneId.systemDefault()), event.isFromClient());
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
