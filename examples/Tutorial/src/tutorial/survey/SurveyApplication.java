package tutorial.survey;

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
 *  @version $Id$
 *  @author Howard Ship
 *
 */ 

import com.primix.tapestry.*;
import com.primix.tapestry.app.*;
import javax.servlet.*;

public class SurveyApplication extends SimpleApplication
{
	private transient SurveyDatabase database;

	public SurveyApplication(RequestContext context)
	{
		super(context, null);
	}

	protected String getSpecificationAttributeName()
	{
		return "Survey.application";
	}
	
	protected String getSpecificationResourceName()
	{
		return "/tutorial/survey/Survey.application";
	}
	
	
	public SurveyDatabase getDatabase()
	{
		return database;
	}
	

	protected void setupForRequest(RequestContext context)
	{
		super.setupForRequest(context);
		
		if (database == null)
		{
			String name = "Survey.database";
			ServletContext servletContext;
			
			servletContext = context.getServlet().getServletContext();
			
			database = (SurveyDatabase)servletContext.getAttribute(name);
			
			if (database == null)
			{
				database = new SurveyDatabase();
				servletContext.setAttribute(name, database);
			}
		}
	}
	
	private static final String[] pageNames = 
	{ "home", "survey", "results" 
	};
	
	public String[] getPageNames()
	{
		return pageNames;
	}
}