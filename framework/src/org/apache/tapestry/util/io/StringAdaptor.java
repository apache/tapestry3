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

package org.apache.tapestry.util.io;

/**
 *  Squeezes a String (which is pretty simple, most of the time).
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *
 **/

class StringAdaptor implements ISqueezeAdaptor
{
    private static final String PREFIX = "S";
    private static final char PREFIX_CH = 'S';

    public void register(DataSqueezer squeezer)
    {
        squeezer.register(PREFIX, String.class, this);
    }

    public String squeeze(DataSqueezer squeezer, Object data)
    {
        String string = (String) data;
        char ch;

        // An empty String is encoded as an 'S', that is, a String with
        // a length of zero.

        if (string.length() == 0)
            return PREFIX;

        ch = string.charAt(0);

        // If the first character of the string is claimed
        // this or some other adaptor, then prefix it
        // with this adaptor's prefix.

        if (ch == PREFIX_CH || squeezer.isPrefixRegistered(ch))
            return PREFIX + string;
        else
            // Otherwise, the string is OK as is.
            return string;
    }

    /**
     *  Strips the prefix from the string.  This method is only
     *  invoked by the {@link DataSqueezer} if the string leads
     *  with its normal prefix (an 'S').
     *
     **/

    public Object unsqueeze(DataSqueezer squeezer, String string)
    {
        if (string.length() == 1)
            return "";

        return string.substring(1);
    }
}