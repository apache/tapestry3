//
// Tapestry Web Application Framework
// Copyright (c) 2000-2002 by Howard Lewis Ship
//
// Howard Lewis Ship
// http://sf.net/projects/tapestry
// mailto:hship@users.sf.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE, as well as License.html. If the license is not
// included with this distribution, you may find a copy at the FSF web
// site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
// Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package net.sf.tapestry.vlib;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.Gesture;
import net.sf.tapestry.IComponent;
import net.sf.tapestry.IEngineServiceView;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.ResponseOutputStream;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.engine.AbstractService;

/**
 *  The external service allows for bookmarking of Persons and Books.
 *  It encodes a page to display (which must implement {@link IExternalPage})
 *  with a primary key of a related object.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 1.0.9
 *
 **/

public class ExternalService extends AbstractService
{
    public static final String SERVICE_NAME = "external";

    public Gesture buildGesture(IRequestCycle cycle, IComponent component, Object[] parameters)
    {
        if (parameters == null || parameters.length != 2)
            throw new ApplicationRuntimeException(
                Tapestry.getString("service-incorrect-parameter-count", SERVICE_NAME, "two"));

        return assembleGesture(cycle, SERVICE_NAME, null, parameters, true);

    }

    public boolean service(IEngineServiceView engine, IRequestCycle cycle, ResponseOutputStream output)
        throws RequestCycleException, ServletException, IOException
    {
        IExternalPage page;

        Object[] parameters = getParameters(cycle);

        if (Tapestry.size(parameters) != 2)
            throw new ApplicationRuntimeException(
            Tapestry.getString("service-incorrect-parameter-count", SERVICE_NAME, "two"));

        String pageName = (String)parameters[0];
        Integer primaryKey = (Integer)parameters[1];

        try
        {
            page = (IExternalPage) cycle.getPage(pageName);
        }
        catch (ClassCastException e)
        {
            throw new ApplicationRuntimeException(
                "Page " + pageName + " may not be used with the " + SERVICE_NAME + " service.");
        }

        page.setup(primaryKey, cycle);

        // We don't invoke page.validate() because the whole point of this
        // service is to allow unknown (fresh) users to jump right
        // to the page.

        // Render the response.

        engine.renderResponse(cycle, output);

        return true;
    }

    public String getName()
    {
        return SERVICE_NAME;
    }

}