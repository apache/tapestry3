/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Tapestry" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Tapestry", nor may "Apache" or "Tapestry" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TAPESTRY CONTRIBUTOR COMMUNITY
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.tapestry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.binding.ExpressionBinding;
import org.apache.tapestry.engine.*;
import org.apache.tapestry.parse.AttributeType;
import org.apache.tapestry.parse.CloseToken;
import org.apache.tapestry.parse.ComponentTemplate;
import org.apache.tapestry.parse.LocalizationToken;
import org.apache.tapestry.parse.OpenToken;
import org.apache.tapestry.parse.TemplateAttribute;
import org.apache.tapestry.parse.TemplateToken;
import org.apache.tapestry.parse.TextToken;
import org.apache.tapestry.parse.TokenType;
import org.apache.tapestry.spec.ComponentSpecification;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Utility class instantiated by {@link org.apache.tapestry.BaseComponent} to
 *  process the component's {@link org.apache.tapestry.parse.ComponentTemplate template},
 *  which involves working through the nested structure of the template and hooking
 *  the various static template blocks and components together using
 *  {@link IComponent#addBody(IRender)} and 
 *  {@link org.apache.tapestry.BaseComponent#addOuter(IRender)}.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.4
 *
 **/

public class BaseComponentTemplateLoader
{
    private static final Log LOG = LogFactory.getLog(BaseComponentTemplateLoader.class);

    private IPageLoader _pageLoader;
    private IRequestCycle _requestCycle;
    private BaseComponent _loadComponent;
    private IPageSource _pageSource;
    private ComponentTemplate _template;
    private IComponent[] _stack;
    private int _stackx = 0;
    private IComponent _activeComponent = null;
    private Set _seenIds = new HashSet();

    /**
     *  A class used with invisible localizations.  Constructed
     *  from a {@link TextToken}.
     * 
     * 
     **/

    private static class LocalizedStringRender implements IRender
    {
        private IComponent _component;
        private IComponentStrings _strings;
        private String _key;
        private Map _attributes;
        private boolean _raw;

        private LocalizedStringRender(IComponent component, LocalizationToken token)
        {
            _component = component;
            _key = token.getKey();
            _raw = token.isRaw();
            _attributes = token.getAttributes();
        }

        public void render(IMarkupWriter writer, IRequestCycle cycle) throws RequestCycleException
        {
            if (cycle.isRewinding())
                return;

            if (_attributes != null)
            {
                writer.begin("span");

                Iterator i = _attributes.entrySet().iterator();

                while (i.hasNext())
                {
                    Map.Entry entry = (Map.Entry) i.next();
                    String attributeName = (String) entry.getKey();
                    String attributeValue = (String) entry.getValue();

                    writer.attribute(attributeName, attributeValue);
                }
            }

            if (_strings == null)
                _strings = _component.getStrings();

            String value = _strings.getString(_key);

            if (_raw)
                writer.printRaw(value);
            else
                writer.print(value);

            if (_attributes != null)
                writer.end();
        }

        public String toString()
        {
            ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);

            builder.append("component", _component);
            builder.append("key", _key);
            builder.append("raw", _raw);
            builder.append("attributes", _attributes);

            return builder.toString();
        }

    }

    public BaseComponentTemplateLoader(
        IRequestCycle requestCycle,
        IPageLoader pageLoader,
        BaseComponent loadComponent,
        ComponentTemplate template,
        IPageSource pageSource)
    {
        _requestCycle = requestCycle;
        _pageLoader = pageLoader;
        _loadComponent = loadComponent;
        _template = template;
        _pageSource = pageSource;

        _stack = new IComponent[template.getTokenCount()];
    }

    public void process() throws PageLoaderException
    {
        int count = _template.getTokenCount();

        for (int i = 0; i < count; i++)
        {
            TemplateToken token = _template.getToken(i);

            TokenType type = token.getType();

            if (type == TokenType.TEXT)
            {
                process((TextToken) token);
                continue;
            }

            if (type == TokenType.OPEN)
            {
                process((OpenToken) token);
                continue;
            }

            if (type == TokenType.CLOSE)
            {
                process((CloseToken) token);
                continue;
            }

            if (type == TokenType.LOCALIZATION)
            {
                process((LocalizationToken) token);
                continue;
            }
        }

        // This is also pretty much unreachable, and the message is kind of out
        // of date, too.

        if (_stackx != 0)
            throw new PageLoaderException(
                Tapestry.getString("BaseComponent.unbalance-open-tags"),
                _loadComponent);

        checkAllComponentsReferenced();
    }

    /**
     *  Adds the token (which implements {@link IRender})
     *  to the active component (using {@link IComponent#addBody(IRender)}),
     *  or to this component {@link #addOuter(IRender)}.
     * 
     *  <p>
     *  A check is made that the active component allows a body.
     * 
     **/

    private void process(TextToken token) throws PageLoaderException
    {
        if (_activeComponent == null)
        {
            _loadComponent.addOuter(token);
            return;
        }

        if (!_activeComponent.getSpecification().getAllowBody())
            throw new BodylessComponentException(_activeComponent);

        _activeComponent.addBody(token);
    }

    private void process(OpenToken token) throws PageLoaderException
    {
        String id = token.getId();
        IComponent component = null;
        String componentType = token.getComponentType();

        if (componentType == null)
            component = getEmbeddedComponent(id);
        else
            component = createImplicitComponent(id, componentType);

        // Make sure the template contains each component only once.

        if (_seenIds.contains(id))
            throw new PageLoaderException(
                Tapestry.getString(
                    "BaseComponent.multiple-component-references",
                    _loadComponent.getExtendedId(),
                    id),
                _loadComponent);

        _seenIds.add(id);

        if (_activeComponent == null)
            _loadComponent.addOuter(component);
        else
        {
            // Note: this code may no longer be reachable (because the
            // template parser does this check first).

            if (!_activeComponent.getSpecification().getAllowBody())
                throw new BodylessComponentException(_activeComponent);

            _activeComponent.addBody(component);
        }

        addTemplateBindings(component, token);

        _stack[_stackx++] = _activeComponent;

        _activeComponent = component;
    }

    private IComponent createImplicitComponent(String id, String componentType)
        throws PageLoaderException
    {
        return _pageLoader.createImplicitComponent(
            _requestCycle,
            _loadComponent,
            id,
            componentType);
    }

    private IComponent getEmbeddedComponent(String id) throws PageLoaderException
    {
        try
        {
            return _loadComponent.getComponent(id);

        }
        catch (NoSuchComponentException ex)
        {
            throw new PageLoaderException(
                Tapestry.getString(
                    "BaseComponent.undefined-embedded-component",
                    _loadComponent.getExtendedId(),
                    id),
                _loadComponent,
                ex);
        }
    }

    private void process(CloseToken token) throws PageLoaderException
    {
        // Again, this is pretty much impossible to reach because
        // the template parser does a great job.

        if (_stackx <= 0)
            throw new PageLoaderException(
                Tapestry.getString("BaseComponent.unbalanced-close-tags"),
                _loadComponent);

        // Null and forget the top element on the stack.

        _stack[_stackx--] = null;

        _activeComponent = _stack[_stackx];
    }

    private void process(LocalizationToken token)
    {
        IRender render = new LocalizedStringRender(_loadComponent, token);

        if (_activeComponent == null)
            _loadComponent.addOuter(render);
        else
            _activeComponent.addBody(render);
    }

    /**
     *  Adds bindings based on attributes in the template.
     * 
     *  @since 2.4
     * 
     **/

    private void addTemplateBindings(IComponent component, OpenToken token)
        throws PageLoaderException
    {
        Map attributes = token.getAttributesMap();

        if (attributes == null)
            return;

        ComponentSpecification spec = component.getSpecification();

        Iterator i = attributes.entrySet().iterator();

        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();

            String name = (String) entry.getKey();
            TemplateAttribute attribute = (TemplateAttribute) entry.getValue();
            AttributeType type = attribute.getType();

            if (type == AttributeType.OGNL_EXPRESSION)
            {
                addExpressionBinding(component, spec, name, attribute.getValue());
                continue;
            }

            if (type != AttributeType.LITERAL)
                throw new PageLoaderException("Unexpected " + type);

            addStaticBinding(component, spec, name, attribute.getValue());
        }
    }

    /**
     *  Adds an expression binding, checking for errors related
     *  to reserved and informal parameters.
     *
     *  <p>It is an error to specify expression 
     *  bindings in both the specification
     *  and the template.
     * 
     *  @since 2.4
     **/

    private void addExpressionBinding(
        IComponent component,
        ComponentSpecification spec,
        String name,
        String expression)
        throws PageLoaderException
    {

        // If matches a formal parameter name, allow it to be set
        // unless there's already a binding.

        boolean isFormal = (spec.getParameter(name) != null);

        if (isFormal)
        {
            if (component.getBinding(name) != null)
                throw new PageLoaderException(
                    Tapestry.getString(
                        "BaseComponent.dupe-template-expression",
                        name,
                        component.getExtendedId(),
                        _loadComponent.getExtendedId()),
                    component);
        }
        else
        {
            if (!spec.getAllowInformalParameters())
                throw new PageLoaderException(
                    Tapestry.getString(
                        "BaseComponent.template-expression-for-informal-parameter",
                        name,
                        component.getExtendedId(),
                        _loadComponent.getExtendedId()),
                    component);

            // If the name is reserved (matches a formal parameter
            // or reserved name, caselessly), then skip it.

            if (spec.isReservedParameterName(name))
                throw new PageLoaderException(
                    Tapestry.getString(
                        "BaseComponent.template-expression-for-reserved-parameter",
                        name,
                        component.getExtendedId(),
                        _loadComponent.getExtendedId()),
                    component);
        }

        IBinding binding =
            new ExpressionBinding(_pageSource.getResourceResolver(), _loadComponent, expression);

        component.setBinding(name, binding);
    }

    /**
     *  Adds a static binding, checking for errors related
     *  to reserved and informal parameters.
     * 
     *  <p>
     *  Static bindings that conflict with bindings in the
     *  specification are quietly ignored.
     *
     *  @since 2.4
     *
     **/

    private void addStaticBinding(
        IComponent component,
        ComponentSpecification spec,
        String name,
        String staticValue)
    {

        if (component.getBinding(name) != null)
            return;

        // If matches a formal parameter name, allow it to be set
        // unless there's already a binding.

        boolean isFormal = (spec.getParameter(name) != null);

        if (!isFormal)
        {
            // Skip informal parameters if the component doesn't allow them.

            if (!spec.getAllowInformalParameters())
                return;

            // If the name is reserved (matches a formal parameter
            // or reserved name, caselessly), then skip it.

            if (spec.isReservedParameterName(name))
                return;
        }

        IBinding binding = _pageSource.getStaticBinding(staticValue);

        component.setBinding(name, binding);
    }

    private void checkAllComponentsReferenced() throws PageLoaderException
    {
        // First, contruct a modifiable copy of the ids of all expected components
        // (that is, components declared in the specification).

        Map components = _loadComponent.getComponents();

        Set ids = components.keySet();

        // If the seen ids ... ids referenced in the template, matches
        // all the ids in the specification then we're fine.

        if (_seenIds.containsAll(ids))
            return;

        // Create a modifiable copy.  Remove the ids that are referenced in
        // the template.  The remainder are worthy of note.

        ids = new HashSet(ids);
        ids.removeAll(_seenIds);

        int count = ids.size();

        String key =
            (count == 1)
                ? "BaseComponent.missing-component-spec-single"
                : "BaseComponent.missing-component-spec-multi";

        StringBuffer buffer =
            new StringBuffer(Tapestry.getString(key, _loadComponent.getExtendedId()));

        Iterator i = ids.iterator();
        int j = 1;

        while (i.hasNext())
        {
            if (j == 1)
                buffer.append(' ');
            else
                if (j == count)
                {
                    buffer.append(' ');
                    buffer.append(Tapestry.getString("BaseComponent.and"));
                    buffer.append(' ');
                }
                else
                    buffer.append(", ");

            buffer.append(i.next());

            j++;
        }

        buffer.append('.');

        LOG.error(buffer.toString());
    }
}