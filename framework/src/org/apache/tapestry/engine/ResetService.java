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

package org.apache.tapestry.engine;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.RequestCycleException;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.request.ResponseOutputStream;

/**
 *  ServiceLink used to discard all cached data (templates, specifications, et cetera).
 *  This is primarily used during development.  It could be a weakness of a Tapestry
 *  application, making it susceptible to denial of service attacks, which is why
 *  it is disabled by default.  The link generated by the ResetService redisplays the
 *  current page after discarding all data.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 1.0.9
 *  @see org.apache.tapestry.IEngine#isResetServiceEnabled()
 * 
 **/

public class ResetService extends AbstractService
{

    public ILink getLink(IRequestCycle cycle, IComponent component, Object[] parameters)
    {
        if (Tapestry.size(parameters) != 0)
            throw new IllegalArgumentException(
                Tapestry.getString("service-no-parameters", Tapestry.RESET_SERVICE));

        String[] context = new String[1];
        context[0] = component.getPage().getPageName();

        return constructLink(cycle, Tapestry.RESET_SERVICE, context, null, true);
    }

    public String getName()
    {
        return Tapestry.RESET_SERVICE;
    }

    public boolean service(
        IEngineServiceView engine,
        IRequestCycle cycle,
        ResponseOutputStream output)
        throws RequestCycleException, ServletException, IOException
    {
        String[] context = getServiceContext(cycle.getRequestContext());

        if (Tapestry.size(context) != 1)
            throw new ApplicationRuntimeException(
                Tapestry.getString("service-single-parameter", Tapestry.RESET_SERVICE));

        String pageName = context[0];

        if (engine.isResetServiceEnabled())
            engine.clearCachedData();

        IPage page = cycle.getPage(pageName);

        page.validate(cycle);

        cycle.setPage(page);

        // Render the same page (that contained the reset link).

        engine.renderResponse(cycle, output);

        return true;
    }

}