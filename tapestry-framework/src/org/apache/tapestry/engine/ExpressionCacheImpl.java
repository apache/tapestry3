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

import ognl.ClassCacheInspector;
import ognl.Node;
import ognl.OgnlRuntime;
import org.apache.tapestry.AbstractComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
public class ExpressionCacheImpl implements ExpressionCache, ClassCacheInspector
{

    private final Map _compiledExpressionCache = new HashMap();

    private final Map _expressionCache = new HashMap();

    private final boolean _cachingDisabled = Boolean.getBoolean("org.apache.tapestry.disable-caching");

    public ExpressionCacheImpl()
    {
        initializeService();
    }

    public void initializeService()
    {
        if (_cachingDisabled)
        {
            OgnlRuntime.setClassCacheInspector(this);
        }
    }

    public synchronized void reset()
    {
        _compiledExpressionCache.clear();
        _expressionCache.clear();
    }

    public boolean shouldCache(Class type)
    {
        if (!_cachingDisabled || type == null
                || AbstractComponent.class.isAssignableFrom(type))
            return false;

        return true;
    }

    public synchronized Object get(Object target, String expression)
    {
        Map cached = (Map) _compiledExpressionCache.get(target.getClass());
        if (cached == null)
        {
            return _expressionCache.get(expression);
        }
        else
        {
            return cached.get(expression);
        }
    }

    public synchronized void cache(Object target, String expression, Node node)
    {
        if (node.getAccessor() != null)
        {
            Map cached = (Map) _compiledExpressionCache.get(target.getClass());

            if (cached == null)
            {
                cached = new HashMap();
                _compiledExpressionCache.put(target.getClass(), cached);
            }

            cached.put(expression, node);

            _expressionCache.remove(target.getClass());
        }
        else
        {
            _expressionCache.put(expression, node);
        }
    }
}
