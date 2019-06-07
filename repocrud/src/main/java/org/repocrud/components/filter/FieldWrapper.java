package org.repocrud.components.filter;

import lombok.AllArgsConstructor;

import java.lang.reflect.Field;

import static org.repocrud.text.LocalText.text;

/**
 * @author Denis B. Kulikov<br/>
 * date: 22.11.2018:9:18<br/>
 */
@AllArgsConstructor
class FieldWrapper {
    private final Field field;

    public Class getType() {
        return field.getType();
    }

    @Override
    public String toString() {
        return text(Field.class, field.getName());
    }
}
