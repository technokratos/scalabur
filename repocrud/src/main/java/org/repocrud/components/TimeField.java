package org.repocrud.components;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;

/**
 * @author Denis B. Kulikov<br/>
 * date: 04.10.2018:0:01<br/>
 */
public class TimeField extends Composite<Div>
        implements HasValue<AbstractField.ComponentValueChangeEvent<TimeField, LocalTime>, LocalTime> {

    private final TextField textField;
    private LocalTime value;

    public TimeField() {
        this((String) null);
    }

    public TimeField(String label) {
        textField = label != null ? new TextField(label): new TextField();
        textField.addValueChangeListener(event -> {
            if (StringUtils.isEmpty(textField.getValue())) {
                value = null;
            } else {
                try {
                    value = LocalTime.parse(textField.getValue());
                } catch (Exception e) {
                    value = null;
                    textField.setValue(null);
                }
            }
        });
        getContent().add(textField);
    }

    public TimeField(LocalTime localTime, String label) {
        this(label);
        textField.setValue(localTime.toString());
        this.value = localTime;
    }

    public TimeField(LocalTime localTime) {
        this((String) null);
        textField.setValue(localTime.toString());
        this.value = localTime;
    }

    @Override
    public void setValue(LocalTime value) {
        this.value = value;
        textField.setValue(value.toString());
    }

    @Override
    public LocalTime getValue() {
        return value;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<TimeField, LocalTime>> listener) {
        return textField.addValueChangeListener(event -> {
            AbstractField.ComponentValueChangeEvent<TimeField, LocalTime> innerEvent =
                    new AbstractField.ComponentValueChangeEvent<>(
                            TimeField.this, TimeField.this,
                            LocalTime.parse(event.getOldValue()), event.isFromClient());
            listener.valueChanged(innerEvent);
        });
    }


    @Override
    public void setReadOnly(boolean readOnly) {
        textField.setReadOnly(true);
    }

    @Override
    public boolean isReadOnly() {
        return textField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return textField.isRequiredIndicatorVisible();
    }

    public static class TimeChangedEvent implements ValueChangeEvent<LocalTime> {


        private final LocalTime oldValue;
        private final LocalTime value;
        private final HasValue<?, LocalTime> hasValue;
        private final boolean fromClient;

        public TimeChangedEvent(LocalTime oldValue, LocalTime value, HasValue<?, LocalTime> hasValue, boolean fromClient) {
            this.oldValue = oldValue;
            this.value = value;
            this.hasValue = hasValue;
            this.fromClient = fromClient;
        }

        @Override
        public HasValue<?, LocalTime> getHasValue() {
            return hasValue;
        }

        @Override
        public boolean isFromClient() {
            return fromClient;
        }

        @Override
        public LocalTime getOldValue() {
            return oldValue;
        }

        @Override
        public LocalTime getValue() {
            return value;
        }
    }
}
