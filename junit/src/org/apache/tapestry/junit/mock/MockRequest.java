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

package org.apache.tapestry.junit.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *  Mock implementation of {@link javax.servlet.http.HttpServletRequest}.
 *
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 2.2
 * 
 **/

public class MockRequest extends AttributeHolder implements HttpServletRequest
{
    /**
     *  Map of String[].
     * 
     **/

    private Map _parameters = new HashMap();

    /**
     *  Map of String[]
     * 
     **/
    
    private Map _headers = new HashMap();
    
    private String _method = "GET";

    private String _contextPath;

    private MockContext _servletContext;
    private MockSession _session;
    private String _servletPath;
    private List _cookies = new ArrayList();
    private String _contentType;
    private String _contentPath;

    public MockRequest(MockContext servletContext, String servletPath)
    {
        _servletContext = servletContext;

        _contextPath = "/" + servletContext.getServletContextName();
        _servletPath = servletPath;
        
        _session = _servletContext.getSession();
    }

    public String getAuthType()
    {
        return null;
    }

    public Cookie[] getCookies()
    {       
        return (Cookie[])_cookies.toArray(new Cookie[_cookies.size()]);
    }

    public long getDateHeader(String arg0)
    {
        return 0;
    }

    public String getHeader(String arg0)
    {
        return null;
    }

    public Enumeration getHeaders(String name)
    {
        String[] headers = (String[])_headers.get(name);
        
        if (headers == null)
            return Collections.enumeration(Collections.EMPTY_LIST);
            
        return Collections.enumeration(Arrays.asList(headers));
    }

    public Enumeration getHeaderNames()
    {
        return getEnumeration(_headers);
    }

    public int getIntHeader(String arg0)
    {
        return 0;
    }

    public String getMethod()
    {
        return _method;
    }

    public String getPathInfo()
    {
        return null;
    }

    public String getPathTranslated()
    {
        return null;
    }

    public String getContextPath()
    {
        return _contextPath;
    }

    public String getQueryString()
    {
        return null;
    }

    public String getRemoteUser()
    {
        return null;
    }

    public boolean isUserInRole(String arg0)
    {
        return false;
    }

    public Principal getUserPrincipal()
    {
        return null;
    }

    public String getRequestedSessionId()
    {
        return null;
    }

    public String getRequestURI()
    {
        return null;
    }

    public StringBuffer getRequestURL()
    {
        return null;
    }

    public String getServletPath()
    {
        return _servletPath;
    }

    public HttpSession getSession(boolean create)
    {
        if (create && _session == null)
            _session = _servletContext.createSession();

        return _session;
    }

    public HttpSession getSession()
    {
        return _session;
    }

    public boolean isRequestedSessionIdValid()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return false;
    }

    public String getCharacterEncoding()
    {
        return null;
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
    {
    }

    public int getContentLength()
    {
        return 0;
    }

    public String getContentType()
    {
        return _contentType;
    }
    
    public void setContentType(String contentType)
    {
    	_contentType = contentType;
    }

    public ServletInputStream getInputStream() throws IOException
    {
 		if (_contentPath == null)
 			return null;
 			
 		return new MockServletInputStream(_contentPath);
    }

    public String getParameter(String name)
    {
        String[] values = getParameterValues(name);

        if (values == null || values.length == 0)
            return null;

        return values[0];
    }

    public Enumeration getParameterNames()
    {
        return Collections.enumeration(_parameters.keySet());
    }

    public String[] getParameterValues(String name)
    {
        return (String[]) _parameters.get(name);
    }

    /** 
     *  Not part of 2.1 API, not used by Tapestry.
     * 
     **/

    public Map getParameterMap()
    {
        return null;
    }

    public String getProtocol()
    {
        return null;
    }

    public String getScheme()
    {
        return "http";
    }

    public String getServerName()
    {
        return "junit-test";
    }

    public int getServerPort()
    {
        return 80;
    }

    public BufferedReader getReader() throws IOException
    {
        return null;
    }

    public String getRemoteAddr()
    {
        return null;
    }

    public String getRemoteHost()
    {
        return null;
    }

    private Locale _locale = Locale.ENGLISH;

    public Locale getLocale()
    {
        return _locale;
    }
    
    public void setLocale(Locale locale)
    {
        _locale = locale;
    }

    public Enumeration getLocales()
    {
        return Collections.enumeration(Collections.singleton(_locale));
    }

    public boolean isSecure()
    {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        return _servletContext.getRequestDispatcher(path);
    }

    public String getRealPath(String arg0)
    {
        return null;
    }

    public void setContextPath(String contextPath)
    {
        _contextPath = contextPath;
    }

    public void setMethod(String method)
    {
        _method = method;
    }

    public void setParameter(String name, String[] values)
    {
        _parameters.put(name, values);
    }

    public void setParameter(String name, String value)
    {
        setParameter(name, new String[] { value });
    }   
    
    public void addCookie(Cookie cookie)
    {
        _cookies.add(cookie);
    }
    
    public void addCookies(Cookie[] cookies)
    {
        if (cookies == null)
            return;
            
        for (int i = 0; i < cookies.length; i++)
            addCookie(cookies[i]);
    }
    
    /**
     *  Delegates this to the {@link org.apache.tapestry.junit.mock.MockSession}, if
     *  it exists.
     * 
     **/
    
    public void simulateFailover()
    {
        if (_session != null)
            _session.simulateFailover();
    }
    
    public String getContentPath()
    {
        return _contentPath;
    }

    public void setContentPath(String contentPath)
    {
        _contentPath = contentPath;
    }

}