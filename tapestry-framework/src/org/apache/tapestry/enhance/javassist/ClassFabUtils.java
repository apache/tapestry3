package org.apache.tapestry.enhance.javassist;

import java.lang.reflect.Method;

/**
 * Static class containing utility methods.
 *
 * @author Howard Lewis Ship
 */
public class ClassFabUtils
{
    private static long _uid = System.currentTimeMillis();

    private static final char QUOTE = '"';

    private ClassFabUtils()
    {
    }

    /**
     * Generates a unique class name, which will be in the default package.
     */

    public static synchronized String generateClassName(String baseName)
    {
        return "$" + baseName + "_" + Long.toHexString(_uid++);
    }

    /**
     * Returns a class name derived from the provided interfaceClass. The package part of the
     * interface name is stripped out, and the result passed to {@link #generateClassName(String)}.
     *
     * @since 1.1
     */

    public static synchronized String generateClassName(Class interfaceClass)
    {
        String name = interfaceClass.getName();

        int dotx = name.lastIndexOf('.');

        return generateClassName(name.substring(dotx + 1));
    }

    /**
     * Javassist needs the class name to be as it appears in source code, even for arrays. Invoking
     * getName() on a Class instance representing an array returns the internal format (i.e, "[...;"
     * or something). This returns it as it would appear in Java code.
     */
    public static String getJavaClassName(Class inputClass)
    {
        if (inputClass.isArray())
            return getJavaClassName(inputClass.getComponentType()) + "[]";

        return inputClass.getName();
    }

    /**
     * Returns true if the method is the standard toString() method. Very few interfaces will ever
     * include this method as part of the interface, but we have to be sure.
     */
    public static boolean isToString(Method method)
    {
        if (!method.getName().equals("toString"))
            return false;

        if (method.getParameterTypes().length > 0)
            return false;

        return method.getReturnType().equals(String.class);
    }
}


