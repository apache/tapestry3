//
// Tapestry Web Application Framework
// Copyright (c) 2002 by Howard Lewis Ship
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

package net.sf.tapestry.junit.mock.simple;

import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.html.BasePage;

/**
 *  Part of the test suite.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.2
 * 
 **/

public class Two extends BasePage
{
    private String _message;
    
    public void detach()
    {
        _message = null;
        
        super.detach();
    }
    
    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    public void go(IRequestCycle cycle)
    {
        setMessage("You clicked the link!");
    }
}