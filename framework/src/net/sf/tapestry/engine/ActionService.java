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
package net.sf.tapestry.engine;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.Gesture;
import net.sf.tapestry.IAction;
import net.sf.tapestry.IComponent;
import net.sf.tapestry.IEngineServiceView;
import net.sf.tapestry.IPage;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.ResponseOutputStream;
import net.sf.tapestry.StaleSessionException;
import net.sf.tapestry.Tapestry;

/**
 *  A context-sensitive service related to {@link net.sf.tapestry.form.Form} 
 *  and {@link net.sf.tapestry.link.ActionLink}.  Encodes
 *  the page, component and an action id in the service context.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 1.0.9
 *
 **/

public class ActionService extends AbstractService
{
    /**
     *  Encoded into URL if engine was stateful.
     * 
     *  @since 2.4
     **/

    private static final String STATEFUL_ON = "1";

    /**
     *  Encoded into URL if engine was not stateful.
     * 
     *  @since 2.4
     **/

    private static final String STATEFUL_OFF = "0";

    public Gesture buildGesture(IRequestCycle cycle, IComponent component, Object[] parameters)
    {
        if (parameters == null || parameters.length != 1)
            throw new IllegalArgumentException(Tapestry.getString("service-single-parameter", ACTION_SERVICE));

        String stateful = cycle.getEngine().isStateful() ? STATEFUL_ON : STATEFUL_OFF;
        IPage componentPage = component.getPage();
        IPage responsePage = cycle.getPage();

        boolean complex = (componentPage != responsePage);

        String[] serviceContext = new String[complex ? 5 : 4];

        int i = 0;

        serviceContext[i++] = stateful;
        serviceContext[i++] = responsePage.getPageName();
        serviceContext[i++] = (String) parameters[0];

        // Because of Block/InsertBlock, the component may not be on
        // the same page as the response page and we need to make
        // allowances for this.

        if (complex)
            serviceContext[i++] = componentPage.getPageName();

        serviceContext[i++] = component.getIdPath();

        return assembleGesture(cycle, ACTION_SERVICE, serviceContext, null, true);
    }

    public boolean service(IEngineServiceView engine, IRequestCycle cycle, ResponseOutputStream output)
        throws RequestCycleException, ServletException, IOException
    {
        IAction action = null;
        String componentPageName;
        int count = 0;

        String[] serviceContext = getServiceContext(cycle.getRequestContext());

        if (serviceContext != null)
            count = serviceContext.length;

        if (count != 4 && count != 5)
            throw new ApplicationRuntimeException(Tapestry.getString("ActionService.context-parameters"));

        boolean complex = count == 5;

        int i = 0;
        String stateful = serviceContext[i++];
        String pageName = serviceContext[i++];
        String targetActionId = serviceContext[i++];

        if (complex)
            componentPageName = serviceContext[i++];
        else
            componentPageName = pageName;

        String targetIdPath = serviceContext[i++];

        IPage page = cycle.getPage(pageName);

        IPage componentPage = cycle.getPage(componentPageName);
        IComponent component = componentPage.getNestedComponent(targetIdPath);

        try
        {
            action = (IAction) component;
        }
        catch (ClassCastException ex)
        {
            throw new RequestCycleException(
                Tapestry.getString("ActionService.component-wrong-type", component.getExtendedId()),
                component,
                ex);
        }

        // Only perform the stateful check if the application was stateful
        // when the URL was rendered.

        if (stateful.equals(STATEFUL_ON) && action.getRequiresSession())
        {
            HttpSession session = cycle.getRequestContext().getSession();

            if (session == null || session.isNew())
                throw new StaleSessionException();
        }

        // Allow the page to validate that the user is allowed to visit.  This is simple
        // protection from malicious users who hack the URLs directly, or make inappropriate
        // use of the back button. 

        // Note that we validate the page that rendered the response which (again, due to
        // Block/InsertBlock) is not necessarily the page that contains the component.

        page.validate(cycle);

        // Setup the page for the rewind, then do the rewind.

        cycle.setPage(page);
        cycle.rewindPage(targetActionId, action);

        // During the rewind, a component may change the page.  This will take
        // effect during the second render, which renders the HTML response.

        // Render the response.

        engine.renderResponse(cycle, output);

        return true;
    }

    public String getName()
    {
        return ACTION_SERVICE;
    }

}