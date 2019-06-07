package org.repocrud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Denis B. Kulikov<br/>
 * date: 14.03.2019:15:27<br/>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckBoxCollection {
    Class<?> type();

    String foreignKey();

    String valueField();
}
