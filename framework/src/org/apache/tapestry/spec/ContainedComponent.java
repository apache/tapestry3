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

package org.apache.tapestry.spec;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.util.BasePropertyHolder;

/**
 * Defines a contained component.  This includes the information needed to
 * get the contained component's specification, as well as any bindings
 * for the component.
 *
 * @author Howard Lewis Ship
 * @version $Id$
 * 
 **/

public class ContainedComponent extends BasePropertyHolder
{
	private String type;

	private String copyOf;

	protected Map bindings;

	private static final int MAP_SIZE = 3;

	/**
	 *  Returns the named binding, or null if the binding does not
	 *  exist.
	 *
	 **/

	public BindingSpecification getBinding(String name)
	{
		if (bindings == null)
			return null;

		return (BindingSpecification) bindings.get(name);
	}

	/**
	 *  Returns an umodifiable <code>Collection</code>
	 *  of Strings, each the name of one binding
	 *  for the component.
	 *
	 **/

	public Collection getBindingNames()
	{
		if (bindings == null)
			return Collections.EMPTY_LIST;

		return Collections.unmodifiableCollection(bindings.keySet());
	}

	public String getType()
	{
		return type;
	}

	public void setBinding(String name, BindingSpecification spec)
	{
		if (bindings == null)
			bindings = new HashMap(MAP_SIZE);

		bindings.put(name, spec);
	}

	public void setType(String value)
	{
		type = value;
	}

	/**
	 * 	Sets the String Id of the component being copied from.
	 *  For use by IDE tools like Spindle.
	 * 
	 *  @since 1.0.9
	 **/

	public void setCopyOf(String id)
	{
		copyOf = id;
	}

	/**
	 * 	Returns the id of the component being copied from.
	 *  For use by IDE tools like Spindle.
	 * 
	 *  @since 1.0.9
	 **/

	public String getCopyOf()
	{
		return copyOf;
	}
}