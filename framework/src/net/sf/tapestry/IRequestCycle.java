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

package net.sf.tapestry;

/**
 *  Controller object that manages a single request cycle.  A request cycle
 *  is one 'hit' on the web server.  In the case of a Tapestry application,
 *  this will involve:
 *  <ul>
 *  <li>Responding to the URL by finding an {@link IEngineService} object
 *  <li>Determining the result page
 *  <li>Renderring the result page
 *  <li>Releasing any resources
 *  </ul>
 *
 *  <p>Mixed in with this is:
 *  <ul>
 *  <li>Exception handling
 *  <li>Loading of pages and templates from resources
 *  <li>Tracking changes to page properties, and restoring pages to prior states
 *  <li>Pooling of page objects
 *  </ul>
 *
 *  <p>A request cycle is broken up into two phases.   The <em>rewind</em> phase
 *  is optional, as it tied to {@link javax.swing.Action} or
 *  {@link net.sf.tapestry.form.Form} components.  In the rewind phase,
 *  a previous page render is redone (discarding output) until a specific component
 *  of the page is reached.  This rewinding ensures that the page
 *  is restored to the exact state it had when the URL for the request cycle was
 *  generated, taking into account the dynamic nature of the page ({@link net.sf.tapestry.components.Foreach},
 *  {@link java.awt.Conditional}, etc.).  Once this component is reached, it can notify
 *  its {@link IActionListener}.  The listener has the ability to update the state
 *  of any pages and select a new result page.
 *
 *  <p>Following the rewind phase is the <em>render</em> phase.  During the render phase,
 *  a page is actually rendered and output sent to the client web browser.
 *
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public interface IRequestCycle
{
    /**
     *  Invoked after the request cycle is no longer needed, to release any resources
     *  it may have.  This includes releasing any loaded pages back to the page source.
     *
     **/

    public void cleanup();

    /**
     *  Passes the String through 
     *  {@link javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)}, which
     *  ensures that the session id is encoded in the URL (if necessary).
     *
     **/

    public String encodeURL(String URL);

    /**
     *  Returns the engine which is processing this request cycle.
     *
     **/

    public IEngine getEngine();

    /**
     *  Retrieves a previously stored attribute, returning null
     *  if not found.  Attributes allow components to locate each other; primarily
     *  they allow a wrapped component to locate a component which wraps it.
     *
     **/

    public Object getAttribute(String name);

    public IMonitor getMonitor();

    /**
     *  Returns the next action id.  Action ids are used to identify different actions on a
     *  page (URLs that are related to dynamic page state).  
     *
     **/

    public String getNextActionId();

    /**
     *  Identifies the page being rendered.
     *
     **/

    public IPage getPage();

    /**
     *  Returns the page with the given name.  If the page has been
     *  previously loaded in the current request cycle, that page is
     *  returned.  Otherwise, the engine's page loader is used to
     *  load the page.
     *
     *  @see IEngine#getPageSource()
     **/

    public IPage getPage(String name);

    public RequestContext getRequestContext();

    /**
     *  Returns true if the context is being used to rewind a prior
     *  state of the page.  This is only true when there is a target
     *  action id.
     *
     **/

    public boolean isRewinding();

    /**
     *  Checks to see if the current action id matches the target
     *  action id.  Returns true only if they match.  Returns false if
     *  there is no target action id (that is, during page rendering).
     *
     *  <p>If theres a match on action id, then the component
     *  is compared against the target component.  If there's a mismatch
     *  then a {@link StaleLinkException} is thrown.
     **/

    public boolean isRewound(IComponent component) throws StaleLinkException;

    /**
     *  Removes a previously stored attribute, if one with the given name exists.
     *
     **/

    public void removeAttribute(String name);

    /**
     *  Renders the given page.  Applications should always use this
     *  method to render the page, rather than directly invoking
     *  {@link IPage#render(IMarkupWriter, IRequestCycle)} since the
     *  request cycle must perform some setup before rendering.
     *
     **/

    public void renderPage(IMarkupWriter writer) throws RequestCycleException;

    /**
     *  Rewinds a page and executes some form of action when the
     *  component with the specified action id is reached.
     *
     *  @see IAction
     *
     **/

    public void rewindPage(String targetActionId, IComponent targetComponent)
        throws RequestCycleException;

    /**
     *  Allows a temporary object to be stored in the request cycle,
     *  which allows otherwise unrelated objects to communicate.  This
     *  is similar to <code>HttpServletRequest.setAttribute()</code>,
     *  except that values can be changed and removed as well.
     *
     *  <p>This is used by components to locate each other.  A component, such
     *  as {@link net.sf.tapestry.html.Body}, will write itself under a well-known name
     *  into the request cycle, and components it wraps can locate it by that name.
     **/

    public void setAttribute(String name, Object value);

    /**
     *  Sets the page to be rendered.  This is called by a component
     *  during the rewind phase to specify an alternate page to render
     *  during the response phase.
     *
     **/

    public void setPage(IPage page);

    /**
     *  Sets the page to be rendered.  This is called by a component
     *  during the rewind phase to specify an alternate page to render
     *  during the response phase.
     *
     **/

    public void setPage(String name);

    /**
     *  Invoked just before rendering the response page to get all
     *  {@link IPageRecorder page recorders} touched in this request cycle
     *  to commit their changes (save them to persistant storage).
     *
     *  @see IPageRecorder#commit()
     **/

    public void commitPageChanges() throws PageRecorderCommitException;

    /**
     *  Returns the service which initiated this request cycle.  This may return
     *  null (very early during the request cycle) if the service has not
     *  yet been determined.
     *
     *  @since 1.0.1
     **/

    public IEngineService getService();

    /**
     *  Used by {@link IForm forms} to perform a <em>partial</em> rewind
     *  so as to respond to the form submission (using the direct service).
     *
     *  @since 1.0.2
     **/

    public void rewindForm(IForm form, String targetActionId) throws RequestCycleException;

    /**
     *  Much like {@link IEngine#forgetPage(String)}, but the page stays active and can even
     *  record changes, until the end of the request cycle, at which point it is discarded
     *  (and any recorded changes are lost).
     *  This is used in certain rare cases where a page has persistent state but is
     *  being renderred "for the last time".
     * 
     *  @since 2.0.2
     * 
     **/

    public void discardPage(String name);
    
    /**
     *  Invoked by a {@link IEngineService service} to store an array of application-specific parameters.
     *  These can later be retrieved (typically, by an application-specific listener method)
     *  by invoking {@link #getServiceParameters()}.
     * 
     *  @see IEngineService#DIRECT_SERVICE
     *  @since 2.0.3
     * 
     **/
    
    public void setServiceParameters(String[] context);
    
    /**
     *  Returns parameters previously stored by {@link #setServiceParameters(String[])}.
     * 
     *  @since 2.0.3
     * 
     **/
    
    public String[] getServiceParameters();
}