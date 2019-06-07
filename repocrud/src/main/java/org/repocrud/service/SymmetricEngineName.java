package org.repocrud.service;

/**
 * @author Denis B. Kulikov<br/>
 * date: 27.03.2019:11:48<br/>
 */

public class SymmetricEngineName {
    private static String engineName = null;
    public static void setEngineName(String engineName) {
        SymmetricEngineName.engineName = engineName;
    }

    public static String getEngineName() {
        return engineName;
    }
}
