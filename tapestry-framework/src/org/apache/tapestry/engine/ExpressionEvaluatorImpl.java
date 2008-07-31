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

import ognl.*;
import ognl.enhance.ExpressionAccessor;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.enhance.IEnhancedClassFactory;
import org.apache.tapestry.enhance.javassist.OGNLExpressionCompiler;
import org.apache.tapestry.spec.IApplicationSpecification;

import java.beans.Introspector;
import java.util.Map;

/**
 * @since 4.0
 */
public class ExpressionEvaluatorImpl implements ExpressionEvaluator
{

    private static final long POOL_MIN_IDLE_TIME = 1000 * 60 * 50;

    private static final long POOL_SLEEP_TIME = 1000 * 60 * 4;

    // Uses Thread's context class loader

    private final ClassResolver _ognlResolver;

    private ExpressionCache _expressionCache;

    private IApplicationSpecification _applicationSpecification;

    private TypeConverter _typeConverter;

    // Context, with a root of null, used when evaluating an expression
    // to see if it is a constant.

    private Map _defaultContext;

    private IEnhancedClassFactory _classFactory;

    private GenericObjectPool _contextPool;

    private final boolean _cachingDisabled = Boolean.getBoolean("org.apache.tapestry.disable-caching");

    private final boolean _compileDisabled = Boolean.getBoolean("org.apache.tapestry.disable-expression-compile");

    public ExpressionEvaluatorImpl(ClassResolver resolver, IEnhancedClassFactory classFactory,
                                   ExpressionCache expressionCache, IApplicationSpecification spec)
    {
        _ognlResolver = resolver;
        _classFactory = classFactory;
        _expressionCache = expressionCache;
        _applicationSpecification = spec;

        initializeService();
    }

    void initializeService()
    {
        if (_applicationSpecification.checkExtension(Tapestry.OGNL_TYPE_CONVERTER))
            _typeConverter = (TypeConverter) _applicationSpecification.getExtension(Tapestry.OGNL_TYPE_CONVERTER,
                                                                                    TypeConverter.class);

        _defaultContext = Ognl.createDefaultContext(null, _ognlResolver, _typeConverter);

        OgnlRuntime.setCompiler(new OGNLExpressionCompiler(_classFactory));

        _contextPool = new GenericObjectPool(new PoolableOgnlContextFactory(_ognlResolver, _typeConverter));

        _contextPool.setMaxActive(-1);
        _contextPool.setMaxIdle(-1);
        _contextPool.setMinEvictableIdleTimeMillis(POOL_MIN_IDLE_TIME);
        _contextPool.setTimeBetweenEvictionRunsMillis(POOL_SLEEP_TIME);
    }

    public Node parse(Object target, String expression)
    {
        Node node = (Node) _expressionCache.get(target, expression);

        if (node == null)
        {
            try
            {
                node = (Node) Ognl.parseExpression(expression);
                _expressionCache.cache(target, expression, node);
            }
            catch (OgnlException ex)
            {
                throw new ApplicationRuntimeException(
                        Tapestry.format("unable-to-read-expression", expression, target, ex), target, null, ex);
            }
        }

        return node;
    }

    public Object read(Object target, String expression)
    {
        Node node = parse(target, expression);

        if (node.getAccessor() != null)
            return read(target, node.getAccessor());

        return readCompiled(target, node);
    }

    public Object readCompiled(Object target, Object expression)
    {
        OgnlContext context = null;
        try
        {
            context = (OgnlContext) _contextPool.borrowObject();
            context.setRoot(target);

            return Ognl.getValue(expression, context, target);
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format("unable-to-read-expression", expression, target, ex),
                                                  target, null, ex);
        }
        finally
        {
            discardContext(context);
        }
    }

    /**
     * Invoked from various finally blocks to discard a OgnlContext used during an operation.
     */
    private void discardContext(OgnlContext context)
    {
        if (context == null) return;

        try
        {
            _contextPool.returnObject(context);
        }
        catch (Exception ex)
        {
            // Ignored.  Should be logged?
        }
    }

    public Object read(Object target, ExpressionAccessor expression)
    {
        OgnlContext context = null;

        try
        {
            context = (OgnlContext) _contextPool.borrowObject();

            return expression.get(context, target);
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format("unable-to-read-expression", expression, target, ex),
                                                  target, null, ex);
        }
        finally
        {
            discardContext(context);
        }
    }

    public void write(Object target, String expression, Object value)
    {
        writeCompiled(target, parse(target, expression), value);
    }

    public void write(Object target, ExpressionAccessor expression, Object value)
    {
        OgnlContext context = null;
        try
        {
            context = (OgnlContext) _contextPool.borrowObject();

            // set up context
            context.setRoot(target);

            expression.set(context, target, value);
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format("unable-to-write-expression",
                                                                  new Object[]{expression, target, value, ex}),
                                                  target, null, ex);
        }
        finally
        {
            discardContext(context);
        }
    }

    public void writeCompiled(Object target, Object expression, Object value)
    {
        OgnlContext context = null;
        try
        {
            context = (OgnlContext) _contextPool.borrowObject();

            Ognl.setValue(expression, context, target, value);
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format("unable-to-write-expression",
                                                                  new Object[]{expression, target, value, ex}),
                                                  target, null, ex);
        }
        finally
        {
            discardContext(context);
        }
    }

    public boolean isConstant(Object target, String expression)
    {
        Node node = parse(target, expression);

        try
        {
            return Ognl.isConstant(node, _defaultContext);
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format("is-constant-expression-error", expression, ex), ex);
        }
    }

    public boolean isCompileEnabled()
    {
        return !_cachingDisabled && !_compileDisabled;
    }

    public void compileExpression(Object target, Node node, String expression)
    {
        OgnlContext context = null;

        try
        {
            context = (OgnlContext) _contextPool.borrowObject();

            // set up context
            context.setRoot(target);

            OgnlRuntime.compileExpression(context, node, target);

            _expressionCache.cache(target, expression, node);

        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(Tapestry.format("unable-to-read-expression", expression, target, ex),
                                                  target, null, ex);
        }
        finally
        {
            discardContext(context);
        }
    }

    public void reset()
    {
        try
        {
            _contextPool.clear();

            OgnlRuntime.clearCache();
            Introspector.flushCaches();
        }
        catch (Exception et)
        {
            // ignore
        }
    }
}