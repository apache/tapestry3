package com.primix.tapestry.components;

import java.io.*;
import com.primix.tapestry.*;
import java.util.*;

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
 *  The body of a Tapestry page.  This is used since it allows components on the
 *  page access to an initialization script (that is written the start, just before
 *  the &lt;body&gt; tag).  This is currently used by {@link Rollover}.
 *
 *  @author Howard Ship
 *  @version $Id$
 */


public class Body extends AbstractComponent
{
    // Lines related to image initialization
	private List imageLines;
	
    // Unique id number, used for naming DOM items in the HTML.

	private int uniqueId;

    // Lines that belong inside the onLoad event handler for the <body> tag.
	private StringBuffer otherInitialization;

    // The writer initially passed to render() ... wrapped elements render
    // into a nested response writer.

    private IResponseWriter outerWriter;
       
    // Set to true when the script element is first opened.
    private boolean openScript;

	private static final String ATTRIBUTE_NAME = 
		"com.primix.tapestry.components.Body";

	private static final String[] reservedNames = { "onLoad" };

	/**
	*  Adds to the script an initialization for the named variable as
	*  an Image(), to the given URL.
	*
	*/

	public void addImageInitialization(String imageName, String URL)
	{
		String variableName;

		if (imageLines == null)
			imageLines = new ArrayList();

		variableName = "tapestry_preload['" + imageName + "']";

		imageLines.add(variableName + " = new Image()");
		imageLines.add(variableName + ".src = \"" + URL + '"');
	}

    /**
     *  Adds other initialization, in the form of additional JavaScript
     *  code to execute from the &lt;body&gt;'s <code>onLoad</code> event
     *  handler.  The caller is responsible for adding a semicolon (statement
     *  terminator).  This method will add a newline after the script.
     *
     */

    public void addOtherInitialization(String script)
    {
        if (otherInitialization == null)
            otherInitialization = new StringBuffer(script.length() + 1);

        otherInitialization.append(script);
        otherInitialization.append('\n');

    }


	/**
	 *  Adds additional scripting code to the page.  This code
	 *  will be added to a large block of scripting code at the
	 *  top of the page (i.e., the before the &lt;body&gt; tag).
	 *
	 *  <p>This is typically used to add some form of JavaScript
	 *  event handler to a page.  For example, the
	 *  {@link Rollover} component makes use of this.
	 *
	 *  <p>The string will be added, as-is, within
	 *  the &lt;script&gt; block generated by this <code>Body</code> component.
	 *  The script should <em>not</em> contain HTML comments, those will
	 *  be supplied by this Body component.
     *
     *  <p>A frequent use is to add an initialization function using
     *  this method, then cause it to be executed using
     *  {@link #addOtherInitialization(String)}.
	 *
	 */
	 
	public void addOtherScript(String script)
	{
        if (!openScript)
            startScript();

        outerWriter.printRaw(script);
	}
		
	 
	/**
	*  Retrieves the <code>Body</code> that was stored into the
	*  request cycle.  This allows components wrapped by the
	*  <code>Body</code> to locate it and access the services it
	*  provides.
	*
	*/

	public static Body get(IRequestCycle cycle)
	{
		return (Body)cycle.getAttribute(ATTRIBUTE_NAME);
	}

	/**
	*  Returns a reference to the initialized image.  The returned
	*  value is a String, suitable for insertion into a script, that
	*  provides the URL of the image.  The value is quoted if that is
	*  necesary.  This localizes how the scripts built by this
	*  component work.
	*
	*/

	public String getInitializedImage(String imageName)
	{
		return "tapestry_preload['" + imageName + "'].src";
	}

	public String getUniqueId()
	{
		return Integer.toString(uniqueId++);
	}

	public void render(IResponseWriter writer, IRequestCycle cycle) 
	throws RequestCycleException
	{
		IResponseWriter nested;
		String onLoadName;

		if (cycle.getAttribute(ATTRIBUTE_NAME) != null)
			throw new RequestCycleException(
				"Body components may not be nested.",
				this, cycle);

		cycle.setAttribute(ATTRIBUTE_NAME, this);

		imageLines = null;
        otherInitialization = null;
		uniqueId = 0;
		outerWriter = writer;
        openScript = false;

		try
		{
			nested = writer.getNestedWriter();

			renderWrapped(nested, cycle);

			// Write the script (i.e., just before the <body> tag).
            // If an onLoad event handler was needed, its name is
            // returned.
			
			onLoadName = writeScript();
			
			// Start the body tag.
			
			writer.begin("body");
			generateAttributes(cycle, writer, reservedNames);

            if (onLoadName != null)
                writer.attribute("onLoad",
                    "javascript:" + onLoadName + "();");

			// Close the nested writer, which dumps its buffered content
			// into its parent.
			
			nested.close();

			writer.end(); // <body>
		}
		finally
		{
			imageLines = null;
			otherInitialization = null;
            outerWriter = null;
            openScript = false;
		}

	}

    private void startScript()
    {
	    outerWriter.begin("script");
	    outerWriter.attribute("language", "javascript");

	    outerWriter.printRaw("<!--\n");

        openScript = true;
    }

    private void endScript()
    {
        if (openScript)
        {
            // Now, close the HTML comment (used to fake out archaic browsers) and
            // the <script> element.`

            outerWriter.printRaw("\n\n// -->");
        
            outerWriter.end(); // <script>
        }

        openScript = false;
    }


	/**
	*  Writes a script that initializes any images and calls any
	*  additional JavaScript functions, as set by {@link
	*  #addImageInitialization(String, String)}.
	*
    *  <p>The script is written just before the &lt;body&gt; tag.
    *
    *  <p>If there are any other initializations 
    * (see {@link #addOtherInitialization(String)}),
    * then a function to execute them is created, and its name
    * is returned.
	*/


	protected String writeScript()
	{
		Iterator i;
        String result = null;

        if (otherInitialization != null)
        {
            if (!openScript)
                startScript();

            result = "onLoad_" + getIdPath().replace('.', '_');

            outerWriter.printRaw("\n\n" +
                            "function " + result + "()\n" +
                            "{\n");

            outerWriter.printRaw(otherInitialization.toString());

            outerWriter.printRaw("}");
        }

        // Write no script if there's no tapestry images or
        // other script initializations.
        
        if (imageLines != null)
        {
            if (!openScript)
                startScript();

        	outerWriter.printRaw("\n\n" +
        	    "var tapestry_preload = new Array();\n" +
                "if (document.images)\n" +
                "{\n");

        	i = imageLines.iterator();
        	while (i.hasNext())
        	{
        		outerWriter.printRaw("  ");
        		outerWriter.printRaw((String)i.next());
        		outerWriter.printRaw(";\n");
        	}

        	outerWriter.printRaw("}");
        }

        endScript();

        return result;
	}
}
