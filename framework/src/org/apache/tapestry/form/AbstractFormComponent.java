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

package org.apache.tapestry.form;

import org.apache.tapestry.AbstractComponent;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IForm;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.valid.IValidationDelegate;

/**
 *  A base class for building components that correspond to HTML form elements.
 *  All such components must be wrapped (directly or indirectly) by
 *  a {@link Form} component.
 *
 *  @version $Id$
 *  @author Howard Lewis Ship
 *  @since 1.0.3
 * 
 **/

public abstract class AbstractFormComponent extends AbstractComponent implements IFormComponent
{
    /**
     *  Returns the {@link Form} wrapping this component.
     *
     *  @throws RequestCycleException if the component is not wrapped by a {@link Form}.
     *
     **/

    public IForm getForm(IRequestCycle cycle)
    {
        IForm result = Form.get(cycle);

        if (result == null)
            throw new ApplicationRuntimeException(
                Tapestry.getString("AbstractFormComponent.must-be-contained-by-form"),
                this);

        return result;
    }

    public IForm getForm()
    {
        return Form.get(getPage().getRequestCycle());
    }

    abstract public String getName();

    /**
     *  Implemented in some subclasses to provide a display name (suitable
     *  for presentation to the user as a label or error message).  This implementation
     *  return null.
     * 
     **/

    public String getDisplayName()
    {
        return null;
    }
    
    /**
     *  Invoked by components (other than {@link org.apache.tapestry.valid.ValidField})
     *  to inform the {@link org.apache.tapestry.valid.IValidationDelegate} for the 
     *  {@link Form}, if any, that it is the current component.  This allows
     *  non-ValidField components to still participate in the validation system,
     *  to a lesser degree.
     * 
     *  @since 2.4
     * 
     **/
    
    protected void updateDelegate(IForm form)
    {
    	IValidationDelegate delegate = form.getDelegate();
    	
    	if (delegate != null)
    		delegate.setFormComponent(this);
    }
}