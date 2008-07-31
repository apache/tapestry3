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

package org.apache.tapestry.enhance.javassist;

import ognl.Node;
import org.apache.tapestry.enhance.IEnhancedClass;
import org.apache.tapestry.enhance.MethodSignature;

/**
 * Simple struct used by {@link OGNLExpressionCompiler} to hold temporary references to
 * all of the objects involved in compiling / generating a compiled ognl expression.
 */
public class CompiledExpression {

    IEnhancedClass _generatedClass;
    Node _expression;
    Object _root;
    MethodSignature _getterMethod;
    MethodSignature _setterMethod;

    public CompiledExpression(Node expression, Object root,
                              MethodSignature getter, MethodSignature setter)
    {
        _expression = expression;
        _root = root;
        _getterMethod = getter;
        _setterMethod = setter;
    }

    public IEnhancedClass getGeneratedClass()
    {
        return _generatedClass;
    }

    public void setGeneratedClass(IEnhancedClass generatedClass)
    {
        _generatedClass = generatedClass;
    }

    public Node getExpression()
    {
        return _expression;
    }

    public void setExpression(Node expression)
    {
        _expression = expression;
    }

    public Object getRoot()
    {
        return _root;
    }

    public void setRoot(Object root)
    {
        _root = root;
    }

    public MethodSignature getGetterMethod()
    {
        return _getterMethod;
    }

    public void setGetterMethod(MethodSignature method)
    {
        _getterMethod = method;
    }

    public MethodSignature getSettermethod()
    {
        return _setterMethod;
    }

    public void setSetterMethod(MethodSignature method)
    {
        _setterMethod = method;
    }

    public String toString()
    {
        return "CompiledExpression[" +
               "_generatedClass=" + _generatedClass +
               '\n' +
               ", _expression=" + _expression +
               '\n' +
               ", _root=" + _root +
               '\n' +
               ", _getterMethod=" + _getterMethod +
               '\n' +
               ", _setterMethod=" + _setterMethod +
               '\n' +
               ']';
    }
}