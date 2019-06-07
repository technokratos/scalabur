package org.repocrud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.repocrud.annotations.NestedAdd.NOTHING;
import static org.repocrud.annotations.NestedView.CRUD;

/**
 * @author Denis B. Kulikov<br/>
 * date: 13.09.2018:20:53<br/>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NestedEntity {
    NestedView view() default CRUD;
    NestedAdd add() default NOTHING;
}
