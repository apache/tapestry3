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

package org.apache.tapestry.junit.valid;

import org.apache.tapestry.junit.TapestryTestCase;
import org.apache.tapestry.valid.StringValidator;
import org.apache.tapestry.valid.ValidationConstraint;
import org.apache.tapestry.valid.ValidatorException;

/**
 *  Tests the {@link StringValidator} class.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 1.0.8
 *
 **/

public class TestStringValidator extends TapestryTestCase
{
    private StringValidator v = new StringValidator();

    public TestStringValidator(String name)
    {
        super(name);
    }

    public void testToString()
    {
        String in = "Foo";
        String out = v.toString(new TestingField("myField"), in);

        assertEquals("Result.", in, out);
    }

    public void testToStringNull()
    {
        String out = v.toString(new TestingField("nullField"), null);

        assertNull("Null expected.", out);
    }

    public void testToObjectRequiredFail()
    {
        v.setRequired(true);

        try
        {
            v.toObject(new TestingField("requiredField"), "");

            fail("Exception expected.");
        }
        catch (ValidatorException ex)
        {
            assertEquals(ValidationConstraint.REQUIRED, ex.getConstraint());
        }
    }

    public void testToObjectRequiredPass() throws ValidatorException
    {
        v.setRequired(true);

        Object result = v.toObject(new TestingField("requiredField"), "stuff");

        assertEquals("Result.", "stuff", result);
    }

    public void testToObjectMinimumFail()
    {
        v.setMinimumLength(10);

        try
        {
            v.toObject(new TestingField("minimum"), "short");

            fail("Exception expected.");
        }
        catch (ValidatorException ex)
        {
            assertEquals(ValidationConstraint.MINIMUM_WIDTH, ex.getConstraint());
        }
    }

    public void testToObjectMinimumPass() throws ValidatorException
    {
        v.setMinimumLength(10);

        String in = "ambidexterous";

        Object out = v.toObject(new TestingField("minimum"), in);

        assertEquals("Result", in, out);
    }

    /**
     *  An empty string is not subject to the minimum length constraint.
     * 
     **/

    public void testToObjectMinimumNull() throws ValidatorException
    {
        v.setMinimumLength(10);

        String in = "";

        Object out = v.toObject(new TestingField("minimum"), in);

        assertNull("Result", out);
    }

    public void testOptional()
    {
        assertEquals(0, StringValidator.OPTIONAL.getMinimumLength());
        assertEquals(false, StringValidator.OPTIONAL.isClientScriptingEnabled());
        assertEquals(false, StringValidator.OPTIONAL.isRequired());
    }

    public void testRequired()
    {
        assertEquals(0, StringValidator.REQUIRED.getMinimumLength());
        assertEquals(false, StringValidator.REQUIRED.isClientScriptingEnabled());
        assertEquals(true, StringValidator.REQUIRED.isRequired());
    }

    public void testSetStaticMinimumLength()
    {
        try
        {
            StringValidator.OPTIONAL.setMinimumLength(1);

            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {
            checkException(ex, "Changes to property values are not allowed.");
        }
    }

    public void testSetStaticClientScripting()
    {
        try
        {
            StringValidator.OPTIONAL.setClientScriptingEnabled(true);

            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {
            checkException(ex, "Changes to property values are not allowed.");
        }
    }

    public void testSetRequired()
    {
        try
        {
            StringValidator.OPTIONAL.setRequired(true);

            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {
            checkException(ex, "Changes to property values are not allowed.");
        }
    }

}