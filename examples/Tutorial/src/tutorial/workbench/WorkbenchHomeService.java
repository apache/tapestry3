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

package tutorial.workbench;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.tapestry.IEngineServiceView;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.ResponseOutputStream;
import net.sf.tapestry.engine.HomeService;

/**
 *  Special version of the home service used to reset the visit tab when re-entering
 *  the Tapestry application from a static HTML page.  
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @see Redirect
 * 
 **/

public class WorkbenchHomeService extends HomeService
{

    public boolean service(IEngineServiceView engine, IRequestCycle cycle, ResponseOutputStream output)
        throws RequestCycleException, ServletException, IOException
    {
        Visit visit = (Visit) engine.getVisit();

        if (visit != null)
            visit.setActiveTabName("Home");

        return super.service(engine, cycle, output);
    }

}