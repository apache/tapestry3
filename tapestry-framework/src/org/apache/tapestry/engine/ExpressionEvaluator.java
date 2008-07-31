//  Copyright 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.engine;

import ognl.Node;
import ognl.enhance.ExpressionAccessor;

/**
 * Wrapper around the OGNL library.
 *
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
public interface ExpressionEvaluator
{

    /**
     * Parses the given ognl expression.
     *
     * @param target
     *          The object the expression is to be executed against.
     * @param expression
     *          The expression.
     *
     * @return The parsed OGNL AST representation of the expression.
     */
    Node parse(Object target, String expression);

    /**
     * Reads a property of the target, defined by the expression.
     *
     * @param target
     *          The object to evaluate the expression against.
     * @param expression
     *          The expression.
     *
     * @return The value returned from the expression statement.
     *
     * @throws org.apache.tapestry.ApplicationRuntimeException
     *             if the expression can not be parsed, or if some other error
     *             occurs during evaluation of the expression.
     */
    Object read(Object target, String expression);

    /**
     * Reads a property of the target, defined by the (previously compiled)
     * expression.
     *
     * @param target
     *          The object to evaluate the expression against.
     * @param expression
     *          The expression.
     *
     * @return The value returned from the expression statement.
     *
     * @throws org.apache.tapestry.ApplicationRuntimeException
     *             if some other error occurs during evaluation of the
     *             expression.
     */
    Object readCompiled(Object target, Object expression);

    /**
     * Reads a property of the target, defined by the (previously compiled)
     * expression.
     *
     * @param target
     *          The object to resolve the expression against.
     * @param expression
     *          The compiled expression.
     * @return
     *          The result of reading on the expression.
     */
    Object read(Object target, ExpressionAccessor expression);

    /**
     * Updates a property of the target, defined by the expression.
     *
     * @param target
     *          The object to evaluate the expression against.
     * @param expression
     *          The expression.
     * @param value
     *          The value to set on the target object.
     *
     * @throws org.apache.tapestry.ApplicationRuntimeException
     *             if the expression can not be parsed, or if some other error
     *             occurs during evaluation of the expression.
     */
    void write(Object target, String expression, Object value);

    /**
     * Updates a property of the target, defined by the (previously compiled)
     * expression.
     *
     * @param target
     *          The object to evaluate the expression against.
     * @param expression
     *          The expression.
     * @param value
     *          The value to set on the target object.
     * 
     * @throws org.apache.tapestry.ApplicationRuntimeException
     *             if some other error occurs during evaluation of the
     *             expression.
     */
    void writeCompiled(Object target, Object expression, Object value);

    /**
     * Updates a property of the target, defined by the (previously compiled)
     * expression.
     *
     * @param target
     *          The target object to set a value on.
     * @param expression
     *          The pre-compiled expression.
     * @param value
     *          The value to set.
     */
    void write(Object target, ExpressionAccessor expression, Object value);

    /**
     * Returns true if the expression evaluates to a constant or other literal
     * value.
     *
     * @param target
     *          The object to evaluate the expression against.
     * @param expression
     *          The expression.
     *
     * @return True if expression represents a constant statement, false otherwise.
     * @throws org.apache.tapestry.ApplicationRuntimeException
     *             if the expression is not valid
     */
    boolean isConstant(Object target, String expression);

    /**
     * Returns true only if both system properties of <code>org.apache.tapestry.disable-caching</code>
     * and <code>org.apache.tapestry.disable-expression-compile</code> are false.
     *
     * @return True if ognl expressions are eligable for JIT compilation.
     */
    boolean isCompileEnabled();

    void compileExpression(Object target, Node node, String expression);

    /**
     * Used to reset any internal state.
     */
    void reset();
}
