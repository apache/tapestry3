/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2002 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache Tapestry" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache Tapestry", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package net.sf.tapestry.inspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.tapestry.BaseComponent;
import net.sf.tapestry.IEngine;
import net.sf.tapestry.IPage;
import net.sf.tapestry.IPageChange;
import net.sf.tapestry.IPageRecorder;
import net.sf.tapestry.event.PageEvent;
import net.sf.tapestry.event.PageRenderListener;

/**
 *  Component of the {@link Inspector} page used to display
 *  the persisent properties of the page.
 *
 *  @version $Id$
 *  @author Howard Lewis Ship
 *
 **/

public class ShowProperties extends BaseComponent implements PageRenderListener
{
    private List _properties;
    private IPageChange _change;
    private IPage _inspectedPage;

    /**
     *  Registers this component as a {@link PageRenderListener}.
     *
     *  @since 1.0.5
     *
     **/

    protected void finishLoad()
    {
        getPage().addPageRenderListener(this);
    }

    /**
     *  Does nothing.
     *
     *  @since 1.0.5
     *
     **/

    public void pageBeginRender(PageEvent event)
    {
    }

    /**
     *  @since 1.0.5
     *
     **/

    public void pageEndRender(PageEvent event)
    {
        _properties = null;
        _change = null;
        _inspectedPage = null;
    }

    private void buildProperties()
    {
        Inspector inspector = (Inspector) getPage();

        _inspectedPage = inspector.getInspectedPage();

        IEngine engine = getPage().getEngine();
        IPageRecorder recorder = engine.getPageRecorder(_inspectedPage.getName());

        // No page recorder?  No properties.

        if (recorder == null)
        {
            _properties = Collections.EMPTY_LIST;
            return;
        }
        
        if (recorder.getHasChanges())
            _properties = new ArrayList(recorder.getChanges());
    }

    /**
     *  Returns a {@link List} of {@link IPageChange} objects.
     *
     *  <p>Sort order is not defined.
     *
     **/

    public List getProperties()
    {
        if (_properties == null)
            buildProperties();

        return _properties;
    }

    public void setChange(IPageChange value)
    {
        _change = value;
    }

    public IPageChange getChange()
    {
        return _change;
    }

    /**
     *  Returns the name of the value's class, if the value is non-null.
     *
     **/

    public String getValueClassName()
    {
        Object value;

        value = _change.getNewValue();

        if (value == null)
            return "<null>";

        return convertClassToName(value.getClass());
    }

    private String convertClassToName(Class cl)
    {
        // TODO: This only handles one-dimensional arrays
        // property.

        if (cl.isArray())
            return "array of " + cl.getComponentType().getName();

        return cl.getName();
    }

}