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

package org.apache.tapestry.junit;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.tapestry.IEngine;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.IResourceResolver;
import org.apache.tapestry.engine.IComponentClassEnhancer;
import org.apache.tapestry.engine.IComponentStringsSource;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.IPageRecorder;
import org.apache.tapestry.engine.IPageSource;
import org.apache.tapestry.engine.IPropertySource;
import org.apache.tapestry.engine.IScriptSource;
import org.apache.tapestry.engine.ISpecificationSource;
import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.request.RequestContext;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.util.io.DataSqueezer;
import org.apache.tapestry.util.pool.Pool;

/**
 *  An implementation of {@link IEngine} used for unit testing.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.0.4
 *
 **/

public class MockEngine implements IEngine
{
    private IComponentStringsSource componentStringsSource;

    private boolean _refreshing;
    private Pool _pool = new Pool();
    private String _servletPath;

    public void forgetPage(String name)
    {
    }

    public Locale getLocale()
    {
        return null;
    }

    public void setLocale(Locale value)
    {
    }

    public IPageRecorder getPageRecorder(String pageName, IRequestCycle cycle)
    {
        return null;
    }

    public IPageRecorder createPageRecorder(String pageName, IRequestCycle cycle)
    {
        return null;
    }

    public IPageSource getPageSource()
    {
        return null;
    }

    public IEngineService getService(String name)
    {
        return null;
    }

    public String getServletPath()
    {
        return _servletPath;
    }

    public String getContextPath()
    {
        return null;
    }

    public IApplicationSpecification getSpecification()
    {
        return null;
    }

    public ISpecificationSource getSpecificationSource()
    {
        return null;
    }

    public ITemplateSource getTemplateSource()
    {
        return null;
    }

    public boolean service(RequestContext context) throws ServletException, IOException
    {
        return false;
    }

    public IResourceResolver getResourceResolver()
    {
        return null;
    }

    public Object getVisit()
    {
        return null;
    }

    public Object getVisit(IRequestCycle cycle)
    {
        return null;
    }

    public void setVisit(Object value)
    {
    }

    public boolean isResetServiceEnabled()
    {
        return false;
    }

    public IScriptSource getScriptSource()
    {
        return null;
    }

    public boolean isStateful()
    {
        return false;
    }

    public IComponentStringsSource getComponentStringsSource()
    {
        return componentStringsSource;
    }

    public void setComponentStringsSource(IComponentStringsSource componentStringsSource)
    {
        this.componentStringsSource = componentStringsSource;
    }

    public DataSqueezer getDataSqueezer()
    {
        return null;
    }

    public boolean isRefreshing()
    {
        return _refreshing;
    }

    public void setRefreshing(boolean refreshing)
    {
        _refreshing = refreshing;
    }

    public IPropertySource getPropertySource()
    {
        return null;
    }

    public Pool getPool()
    {
        return _pool;
    }

    public Object getGlobal()
    {
        return null;
    }

    public IComponentClassEnhancer getComponentClassEnhancer()
    {
        return null;
    }

    public void setServletPath(String servletPath)
    {
        _servletPath = servletPath;
    }

}