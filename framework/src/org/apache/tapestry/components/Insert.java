/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2004 The Apache Software Foundation.  All rights
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

package org.apache.tapestry.components;

import java.text.Format;

import org.apache.tapestry.AbstractComponent;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.Tapestry;

/**
 *  Used to insert some text (from a parameter) into the HTML.
 *
 *  [<a href="../../../../../ComponentReference/Insert.html">Component Reference</a>]
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public abstract class Insert extends AbstractComponent
{
    public abstract IBinding getFormatBinding();

    /**
     *  Prints its value parameter, possibly formatted by its format parameter.
     *
     **/

    protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle)
    {
        if (cycle.isRewinding())
            return;

        Object value = getValue();

        if (value == null)
            return;

        String insert = null;

        Format format = getFormat();

        if (format == null)
        {
            insert = value.toString();
        }
        else
        {
            try
            {
                insert = format.format(value);
            }
            catch (Exception ex)
            {
                throw new ApplicationRuntimeException(
                    Tapestry.format("Insert.unable-to-format", value),
                    this,
                    getFormatBinding().getLocation(),
                    ex);
            }
        }

        String styleClass = getStyleClass();

        if (styleClass != null)
        {
            writer.begin("span");
            writer.attribute("class", styleClass);

            renderInformalParameters(writer, cycle);
        }

        if (getRaw())
            writer.printRaw(insert);
        else
            writer.print(insert);

        if (styleClass != null)
            writer.end(); // <span>
    }

    public abstract Object getValue();

    public abstract Format getFormat();

    public abstract String getStyleClass();

    public abstract boolean getRaw();

}