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

package org.apache.tapestry.link;

import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.RequestCycleException;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.components.ILinkComponent;
import org.apache.tapestry.engine.ILink;

/**
 *  Default implementation of {@link org.apache.tapestry.link.ILinkRenderer}, which
 *  does nothing special.  Can be used as a base class to provide
 *  additional handling.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.4
 **/

public class DefaultLinkRenderer implements ILinkRenderer
{
    /**
     *  A shared instance used as a default for any link that doesn't explicitly
     *  override.
     * 
     **/

    public static final ILinkRenderer SHARED_INSTANCE = new DefaultLinkRenderer();

    public void renderLink(IMarkupWriter writer, IRequestCycle cycle, ILinkComponent linkComponent)
        throws RequestCycleException
    {
        IMarkupWriter wrappedWriter = null;

        if (cycle.getAttribute(Tapestry.LINK_COMPONENT_ATTRIBUTE_NAME) != null)
            throw new RequestCycleException(
                Tapestry.getString("AbstractLinkComponent.no-nesting"),
                linkComponent);

        cycle.setAttribute(Tapestry.LINK_COMPONENT_ATTRIBUTE_NAME, linkComponent);

        boolean disabled = linkComponent.isDisabled();

        if (!disabled)
        {
            ILink l = linkComponent.getLink(cycle);

            writer.begin("a");
            writer.attribute("href", constructURL(l, linkComponent.getAnchor(), cycle));

            beforeBodyRender(writer, cycle, linkComponent);

            // Allow the wrapped components a chance to render.
            // Along the way, they may interact with this component
            // and cause the name variable to get set.

            wrappedWriter = writer.getNestedWriter();
        }
        else
            wrappedWriter = writer;

        linkComponent.renderBody(wrappedWriter, cycle);

        if (!disabled)
        {
            afterBodyRender(writer, cycle, linkComponent);

            linkComponent.renderAdditionalAttributes(writer, cycle);

            wrappedWriter.close();

            // Close the <a> tag

            writer.end();
        }

        cycle.removeAttribute(Tapestry.LINK_COMPONENT_ATTRIBUTE_NAME);
    }

    /**
     *  Converts the EngineServiceLink into a URI or URL.  This implementation
     *  simply invokes {@link ILink#getURL(String, boolean)}.
     * 
     **/

    protected String constructURL(ILink link, String anchor, IRequestCycle cycle)
    {
        return link.getURL(anchor, true);
    }

    /**
     *  Invoked after the href attribute has been written but before
     *  the body of the link is rendered (but only if the link
     *  is not disabled).
     * 
     *  <p>
     *  This implementation does nothing.
     * 
     **/

    protected void beforeBodyRender(IMarkupWriter writer, IRequestCycle cycle, ILinkComponent link)
    {
    }

    /**
     *  Invoked after the body of the link is rendered, but before
     *  {@link ILinkComponent#renderAdditionalAttributes(IMarkupWriter, IRequestCycle)} is invoked
     *  (but only if the link is not disabled).
     * 
     *  <p>
     *  This implementation does nothing.
     * 
     **/

    protected void afterBodyRender(IMarkupWriter writer, IRequestCycle cycle, ILinkComponent link)
    {
    }
}