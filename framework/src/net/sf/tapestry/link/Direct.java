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

package net.sf.tapestry.link;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import net.sf.tapestry.BindingException;
import net.sf.tapestry.IBinding;
import net.sf.tapestry.IDirect;
import net.sf.tapestry.IDirectListener;
import net.sf.tapestry.IEngineService;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.RequiredParameterException;
import net.sf.tapestry.StaleSessionException;

/**
 *  A component for creating a link using the direct service; used for actions that
 *  are not dependant on dynamic page state.
 *
 *
 * <table border=1>
 * <tr> <th>Parameter</th> <th>Type</th> <th>Read / Write </th> <th>Required</th> 
 * <th>Default</th> <th>Description</th>
 * </tr>
 * <tr>
 *  <td>listener</td> <td>{@link IDirectListener}</td>
 *  <td>R</td>
 *  <td>yes</td> <td>&nbsp;</td>
 *  <td>Specifies an object that is notified when the link is clicked.</td> </tr>
 *
 *  <tr>
 *  <td>context</td>
 *  <td>String[] <br> List (of String) <br> String <br>Object</td>
 *  <td>R</td>
 *  <td>no</td>
 *  <td>&nbsp;</td>
 *  <td>An array of Strings to be encoded into the URL.  These parameters will
 *  be decoded when the link is triggered.
 *  <p>If the context is simply an Object, then <code>toString()</code> is invoked on
 it.  It is assumed that the listener will be able to convert it back.
 *  <p>In a web application built onto of Enterprise JavaBeans, the context is
 *  often the primary key of some Entity bean; typically such keys are Strings or
 *  Integers (which can be freely converted from String to Integer by the listener).</td>
 * </tr>
 *
 * <tr>
 *   <td>disabled</td> <td>boolean</td> <td>R</td> <td>no</td> <td>true</td>
 *   <td>Controls whether the link is produced.  If disabled, the portion of the template
 *  the link surrounds is still rendered, but not the link itself.
 *  </td></tr>
 *
 *
 * <tr>
 *	<td>stateful</td>
 *  <td>boolean</td>
 *	<td>R</td>
 *	<td>no</td>
 *	<td>true</td>
 *	<td>If true (the default), then the component requires an active (i.e., non-new)
 * {@link HttpSession} when triggered.  Failing that, it throws a {@link StaleLinkException}.
 *  If false, then no check is necessary.  The latter works well with links that
 * encode all necessary state inside the URL itself.</td>
 * </tr>
 *
 * <tr>
 *		<td>scheme</td>
 *		<td>java.lang.String</td>
 *		<td>R</td>
 *		<td>no</td>
 *		<td>&nbsp;</td>
 *		<td>If specified, then a longer URL (including scheme, server and possibly port)
 * is generated using the specified scheme. Server is determined fromt he incoming request,
 * and port is deterimined from the port paramter or the incoming request.
 *  </td>
 * </tr>
 *
 * <tr>
 *		<td>port</td>
 *		<td>int</td>
 *		<td>R</td>
 *		<td>no</td>
 *		<td>&nbsp;</td>
 *		<td>If specified, then a longer URL (including scheme, server and port)
 *  is generated using the specified port.  The server is determined from the incoming
 *  request, the scheme from the scheme paramter or the incoming request.
 * </td>
 * </tr>
 *
 * <tr>
 *		<td>anchor</td>
 *		<td>java.lang.String</td>
 *		<td>R</td>
 *		<td>no</td>
 *		<td>&nbsp;</td>
 *		<td>The name of an anchor or element to link to.  The final URL will have '#'
 *   and the anchor appended to it.
 * </td> </tr>
 *
 * </table>
 *
 * <p>Informal parameters are allowed.
 *
 * @author Howard Lewis Ship
 * @version $Id$
 *
 **/

public class Direct extends GestureLink implements IDirect
{
    private IBinding listenerBinding;
    private IBinding contextBinding;
    private IBinding statefulBinding;
    private boolean staticStateful;
    private boolean statefulValue;

    public void setContextBinding(IBinding value)
    {
        contextBinding = value;
    }

    public IBinding getContextBinding()
    {
        return contextBinding;
    }

    public void setStatefulBinding(IBinding value)
    {
        statefulBinding = value;

        staticStateful = value.isStatic();
        if (staticStateful)
            statefulValue = value.getBoolean();
    }

    public IBinding getStatefulBinding()
    {
        return statefulBinding;
    }

    /**
     *  Returns true if the stateful parameter is bound to
     *  a true value.  If stateful is not bound, also returns
     *  the default, true.
     *
     **/

    public boolean isStateful()
    {
        if (staticStateful)
            return statefulValue;

        if (statefulBinding != null)
            return statefulBinding.getBoolean();

        return true;
    }

    /**
     *  Returns {@link IEngineService#DIRECT_SERVICE}.
     *
     **/

    protected String getServiceName()
    {
        return IEngineService.DIRECT_SERVICE;
    }

    protected String[] getContext(IRequestCycle cycle)
    {
        return getContext(contextBinding);
    }

    /**
     *  Converts a binding to a context (an array of Strings).
     *  This is used by the {@link Direct} and {@link Service}
     *  components.
     *
     **/

    public static String[] getContext(IBinding binding)
    {
        Object raw;
        String[] context;
        Vector v;

        if (binding == null)
            return null;

        raw = binding.getObject();

        if (raw == null)
            return null;

        if (raw instanceof String[])
            return (String[]) raw;

        if (raw instanceof String)
        {
            context = new String[1];
            context[0] = (String) raw;

            return context;
        }

        if (raw instanceof List)
        {
            List list = (List) raw;

            context = new String[list.size()];

            return (String[]) list.toArray(context);
        }

        // Allow simply Object ... use toString() to make it a string.
        // The listener should be able to convert it back.  For example,
        // if the real type is java.lang.Integer, it's easy to convert
        // it to an int or java.lang.Integer.

        context = new String[1];
        context[0] = raw.toString();

        return context;
    }

    /**
     *  Invoked by the direct service to trigger the application-specific
     *  action by notifying the {@link IDirectListener listener}.
     *
     *  @throws StaleSessionException if the component is stateful, and
     *  the session is new.
     **/

    public void trigger(IRequestCycle cycle, String[] context)
        throws RequestCycleException
    {
        IDirectListener listener;

        if (isStateful())
        {
            HttpSession session = cycle.getRequestContext().getSession();

            if (session == null || session.isNew())
                throw new StaleSessionException();
        }

        listener = getListener(cycle);

        listener.directTriggered(this, context, cycle);
    }

    public IBinding getListenerBinding()
    {
        return listenerBinding;
    }

    public void setListenerBinding(IBinding value)
    {
        listenerBinding = value;
    }

    private IDirectListener getListener(IRequestCycle cycle)
        throws RequestCycleException
    {
        IDirectListener result;

        try
        {
            result =
                (IDirectListener) listenerBinding.getObject("listener", IDirectListener.class);

        }
        catch (BindingException ex)
        {
            throw new RequestCycleException(this, ex);
        }

        if (result == null)
            throw new RequiredParameterException(this, "listener", listenerBinding);

        return result;
    }

}