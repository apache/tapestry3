package com.primix.tapestry;

import com.primix.tapestry.components.*;

/*
 * Tapestry Web Application Framework
 * Copyright (c) 2000 by Howard Ship and Primix Solutions
 *
 * Primix Solutions
 * One Arsenal Marketplace
 * Watertown, MA 02472
 * http://www.primix.com
 * mailto:hship@primix.com
 * 
 * This library is free software.
 * 
 * You may redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation.
 *
 * Version 2.1 of the license should be included with this distribution in
 * the file LICENSE, as well as License.html. If the license is not
 * included with this distribution, you may find a copy at the FSF web
 * site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
 * Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 */

/**
 * Controller object that manages a single request cycle.  A request cycle
 * is one 'hit' on the web server.  In the case of a Tapestry application,
 * this will involve:
 * <ul>
 * <li>Responding to the URL by finding an {@link IApplicationService} object
 * <li>Determining the result page
 * <li>Renderring the result page
 * <li>Releasing any resources
 * </ul>
 *
 * <p>Mixed in with this is:
 * <ul>
 * <li>Exception handling
 * <li>Loading of pages and templates from resources
 * <li>Tracking changes to page properties, and restoring pages to prior states
 * <li>Pooling of page objects
 * </ul>
 *
 * <p>A request cycle is broken up into two phases.   The <em>rewind</em> phase
 * is optional, as it tied to {@link Action} or
 * {@link Form} components.  In the rewind phase,
 * a previous page render is redone (discarding output) until a specific component
 * of the page is reached.  This rewinding ensures that the page
 * is restored to the exact state it had when the URL for the request cycle was
 * generated, taking into account the dynamic nature of the page ({@link Foreach},
 * {@link Conditional}, etc.).  Once this component is reached, it can notify
 * its {@link IActionListener}.  The listener has the ability to update the state
 * of any pages and select a new result page.
 *
 * <p>Following the rewind phase is the <em>result</em> phase.  During the render phase,
 * a page is actually renderred and output sent to the client web browser.
 *
 *
 * @author Howard Ship
 * @version $Id$
 */


public interface IRequestCycle
{
    /**
     *  Invoked after the request cycle is no longer needed, to release any resources
     *  it may have.  This includes releasing any loaded pages back to the page loader.
     *
     */
 
    public void cleanup();

    /**
     *  Passes the String through <code>HttpServletResponse.encodeURL()</code>, which
     *  ensures that the session id is encoded in the URL (if necessary).
     *
     */
 
    public String encodeURL(String URL);

    public IApplication getApplication();
	
    /**
     *  Retrieves a previously stored attribute, returning null
     *  if not found.
     *
     */
 
    public Object getAttribute(String name);

    public IMonitor getMonitor();

    /**
     *  Returns the next action id.  Action ids are used to identify different actions on a
     *  page (URLs that are related to dynamic page state).  They are also used as names
     *  of form elements and even values (for &lt;option&gt; or radio buttons).
     *
     */
 
    public String getNextActionId();
 
    /**
     *  Identifies the page being rendered.
     *
     */
 
    public IPage getPage();

    /**
     *  Returns the page with the given name.  If the page has been
     *  previously loaded in the current request cycle, that page is
     *  returned.  Otherwise, the application's page loader is used to
     *  load the page.
     *
     *  @see IApplication#getPageSource()
     */
 
    public IPage getPage(String name);

    public RequestContext getRequestContext();

    /**
     *  Returns true if the context is being used to rewind a prior
     *  state of the page.  This is only true when there is a target
     *  action id.
     *
     */
 
    public boolean isRewinding();

    /**
     *  Checks to see if the current action id matches the target
     *  action id.  Returns true only if they match.  Returns false if
     *  there is no target action id (that is, during page rendering).
     *
     */
 
    public boolean isRewound();
    
    /**
 	 *  Removes a previously stored attribute, if one with the given name exists.
	 *
	 */
 
    public void removeAttribute(String name);

    /**
     *  Renders the given page.  Applications should always use this
     *  method to render the page, rather than directly invoking
     *  {@link IPage#render(IResponseWriter, IRequestCycle)} since the
     *  request cycle must perform some setup before rendering.
     *
     */
 
    public void renderPage(IResponseWriter writer)
        throws RequestCycleException;

    /**
     *  Rewinds a page and executes some form of action when the
     *  component with the specified action id is reached.
     *
     *  @see Action
     *
     */
 
    public void rewindPage(String targetActionId)
        throws RequestCycleException;

    /**
     *  Allows a temporary object to be stored in the request cycle,
     *  which allows otherwise unrelated objects to communicate.  This
     *  is similar to <code>HttpServletRequest.setAttribute()</code>,
     *  except that values can be changed and removed as well.
     *
     */
 
    public void setAttribute(String name, Object value);

    /**
     *  Sets the page to be rendered.  This is called by a component
     *  during the rewind phase to specify an alternate page to render
     *  during the response phase.
     *
     */
 
    public void setPage(IPage page);

    /**
     *  Sets the page to be rendered.  This is called by a component
     *  during the rewind phase to specify an alternate page to render
     *  during the response phase.
     *
     */
 
    public void setPage(String name);
	
	/**
	 *  Invoked just before renderring the response page to get all
	 *  {@link IPageRecorder page recorders} touched in this request cycle
	 *  to commit their changes (save them to persistant storage and increment
	 *  their version numbers).
	 *
	 *  @see IPageRecorder#commit()
	 */
	 
	public void commitPageChanges()
		throws PageRecorderCommitException;
}
