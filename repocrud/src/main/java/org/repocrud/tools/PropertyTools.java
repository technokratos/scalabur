package org.repocrud.tools;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Denis B. Kulikov<br/>
 * date: 14.02.2019:21:15<br/>
 */
@Slf4j
public class PropertyTools {

    public static Object read(Object obj, Method readMethod) {
        try {
            return readMethod.invoke(obj);
        } catch (IllegalAccessException|InvocationTargetException e) {
            log.error("Error in read obj " + obj + "," + readMethod, e );
        }
        return null;
    }

    public static void write(Object obj, Method writeMethod, Object value) {
        try {
            writeMethod.invoke(obj, value);
        } catch (IllegalAccessException|InvocationTargetException e) {
            log.error("Error in write obj " + obj + "," + writeMethod, e );
        }
    }

}
