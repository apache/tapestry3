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
 *  A runtime exception thrown when an {@link IComponent} is asked for a contained
 *  component that does not exist.
 *
 * @author Howard Ship
 * @version $Id$
 */


public class NoSuchComponentException extends RuntimeException
{
	private String name;
	private transient IComponent container;

	public NoSuchComponentException(String name, IComponent container)
	{
		this.name = name;
		this.container = container;
	}

	public IComponent getContainer()
	{
		return container;
	}

	public String getName()
	{
		return name;
	}
}
