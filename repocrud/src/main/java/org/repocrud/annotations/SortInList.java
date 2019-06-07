package org.repocrud.annotations;

/**
 * @author Denis B. Kulikov<br/>
 * date: 14.02.2019:12:06<br/>
 */

import org.springframework.data.domain.Sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SortInList {
    String value();
    Sort.Direction direction() default Sort.Direction.ASC;
}
