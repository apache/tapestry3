package org.apache.tapestry.engine;

import ognl.Node;

/**
 * Cache of compiled OGNL expressions.
 *
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
public interface ExpressionCache
{
    /**
     * Returns the compiled ognl expression for the given target object class / expression
     * combination.
     *
     * @param target
     *          The object this expression is to be used for.
     * @param expression
     *          The expression.
     * @return
     *      The compiled (or new if neccessary) ognl statement.
     */
    Object get(Object target, String expression);

    /**
     * Stores a parsed ognl expression for the given object type.
     *
     * @param target
     *          The target object expression compiled for.
     * @param expression
     *          The ognl string expression node represents.
     * @param node
     *          The parsed OGNL expression to cache.
     */
    void cache(Object target, String expression, Node node);

    /**
     * Invoked by {@link AbstractEngine} to clear all cache data.
     */
    void reset();
}