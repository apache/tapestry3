package com.primix.tapestry;

import com.primix.tapestry.*;
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
 *  Exception thown when an {@link IBinding} required by a component does not
 *  exist, or when the value for the binding is null (and the component
 *  requires a non-null value).
 *
 *  @author Howard Ship
 *  @version $Id$
 */


public class RequiredParameterException extends RequestCycleException
{
	private String parameterName;
	private transient IBinding binding;

	public RequiredParameterException(IComponent component, String parameterName,
		IBinding binding, IRequestCycle cycle)
	{
		super("No binding or value for parameter " + parameterName + 
			  " in component " + component.getExtendedId() + ".", 
			  component, cycle);

		this.parameterName = parameterName;
		this.binding = binding;
	}

	public String getParameterName()
	{
		return parameterName;
	}
	
	public IBinding getBinding()
	{
		return binding;
	}
}

