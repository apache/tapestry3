/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2002 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache Tapestry" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache Tapestry", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package net.sf.tapestry;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

/**
 * Abstract base class implementing the {@link IMarkupWriter} interface.
 * This class is used to create a Generic Tag Markup Language (GTML) output.   
 * It is more sophisticated than <code>PrintWriter</code> in that it maintains   
 * a concept hierarchy of open GTML tags. It also supplies a number of other
 * of the features that are useful when creating GTML.
 *
 * Elements are started with the {@link #begin(String)} 
 * or {@link #beginEmpty(String)}
 * methods. Once they are started, attributes for the elements may be set with
 * the various <code>attribute()</code> methods. The element is closed off
 * (i.e., the closing '&gt;' character is written) when any other method
 * is invoked (exception: methods which do not produce output, such as
 * {@link #flush()}). The <code>end()</code> methods end an element,
 * writing an GTML close tag to the output.
 *
 * <p>TBD:
 * <ul>
 * <li>Support XML and XHTML
 *  <li>What to do with Unicode characters with a value greater than 255?
 * </ul>
 *
 * <p>This class is derived from the original class 
 * <code>com.primix.servlet.HTMLWriter</code>,
 * part of the <b>ServletUtils</b> framework available from
 * <a href="http://www.gjt.org/servlets/JCVSlet/list/gjt/com/primix/servlet">The Giant 
 * Java Tree</a>.
 *
 * @version $Id$
 * @author Howard Ship, David Solis
 * @since 0.2.9
 *
 **/

public abstract class AbstractMarkupWriter implements IMarkupWriter
{
    /**
     * The underlying {@link PrintWriter} that output is sent to. 
     *  
     **/

    private PrintWriter _writer;

    /**
     * Indicates whether a tag is open or not. A tag is opened by
     * {@link #begin(String)} or {@link #beginEmpty(String)}.
     * It stays open while calls to the <code>attribute()</code>
     * methods are made. It is closed
     * (the '&gt;' is written) when any other method is invoked.
     *
     **/

    private boolean _openTag = false;

    /**
     *  Indicates that the tag was opened with 
     *  {@link #beginEmpty(String)}, which affects
     *  how the tag is closed (a slash is added to indicate the
     *  lack of a body).  This is compatible with HTML, but reflects
     *  an XML/XHTML leaning.
     * 
     *  @since 2.2
     * 
     **/

    private boolean _emptyTag = false;

    /**
     * A Stack of Strings used to track the active tag elements. Elements are active
     * until the corresponding close tag is written.  The {@link #push(String)} method
     * adds elements to the stack, {@link #pop()} removes them.
     *
     **/

    private Stack _activeElementStack;

    /**
     * The depth of the open tag stack.
     * @see #activeElementStack
     *
     **/

    private int _depth = 0;

    private char[] _buffer;

    private String[] _entities;
    private boolean[] _safe;

    /**
     *  Implemented in concrete subclasses to provide an indication of which
     *  characters are 'safe' to insert directly into the response.  The index
     *  into the array is the character, if the value at the index is false (or the
     *  index out of range), then the character is escaped.
     *
     **/

    private String _contentType;

    public String getContentType()
    {
        return _contentType;
    }

    abstract public IMarkupWriter getNestedWriter();

    /**
     *  General constructor used by subclasses.
     * 
     *  @param safe an array of flags indicating which characters
     *  can be passed directly through with out filtering.  Characters marked
     *  unsafe, or outside the range defined by safe, are converted to entities.
     *  @param entities a set of prefered entities, unsafe characters with
     *  a defined entity use the entity, other characters are converted
     *  to numeric entities.
     *  @param contentType the type of content produced by the
     *  writer.
     *  @param outputStream stream to which content will be written.
     *
     **/

    protected AbstractMarkupWriter(
        boolean safe[],
        String[] entities,
        String contentType,
        OutputStream stream)
    {
        this(safe, entities, contentType);

		OutputStreamWriter owriter = new OutputStreamWriter(stream);
		Writer bwriter = new BufferedWriter(owriter);

        _writer = new PrintWriter(bwriter);
    }

    /**
     *  Special constructor used for nested response writers.
     *  The subclass is responsible for creating the writer.
     * 
     **/

    protected AbstractMarkupWriter(boolean safe[], String[] entities, String contentType)
    {
        _entities = entities;
        _safe = safe;
        _contentType = contentType;

        if (entities == null || safe == null || contentType == null)
            throw new IllegalArgumentException(
                Tapestry.getString("AbstractMarkupWriter.missing-constructor-parameters"));

    }

    protected void setWriter(PrintWriter writer)
    {
        _writer = writer;
    }

    /**
     * Simply prints the attribute name. This is used for
     * idempotent attributes, such as 'disabled' in an
     * &lt;input&gt;.
     *
     * <p>TBD: Check that name is legal.
     *
     *  @throws IllegalStateException if there is no open tag.
     *  @deprecated To be removed in 2.3.  Use
     *  {@link #attribute(String, String)} instead.
     **/

    public void attribute(String name)
    {
        attribute(name, name);
    }

    /**
     * Writes an integer attribute into the currently open tag.
     *
     * <p>TBD: Validate that name is legal.
     *
     * @throws IllegalStateException if there is no open tag.
     *
     **/

    public void attribute(String name, int value)
    {
        checkTagOpen();

        _writer.print(' ');
        _writer.print(name);
        _writer.print("=\"");
        _writer.print(value);
        _writer.print('"');
    }

    /**
     *  Writes an attribute into the most recently opened tag. This must be called after
     *  {@link #begin(String)}
     *  and before any other kind of writing (which closes the tag).
     *
     *  <p>The value may be null, in which case this method behaves the same as
     *  {@link #attribute(String)}.
     *
     *  <p>Troublesome characters in the value are converted to thier GTML entities, much
     *  like a <code>print()</code> method, with the following exceptions:
     *  <ul>
     *  <li>The double quote (&quot;) is converted to &amp;quot;
     *  <li>The ampersand (&amp;) is passed through unchanged
     *  </ul>
     *
     *  @throws IllegalStateException if there is no open tag.
     *  @param name The name of the attribute to write (no validation
     *  is done on the name).
     *  @param value The value to write.  If null, the attribute
     *  name is written as the value.  Otherwise, the
     *  value is written, 
     **/

    public void attribute(String name, String value)
    {
        checkTagOpen();

        _writer.print(' ');

        // Could use a check here that name contains only valid characters

        _writer.print(name);
        _writer.print("=\"");

        if (value == null)
        {
            _writer.print(name);
        }
        else
        {

            int length = value.length();

            if (_buffer == null || _buffer.length < length)
                _buffer = new char[length];

            value.getChars(0, length, _buffer, 0);

            safePrint(_buffer, 0, length, true);
        }

        _writer.print('"');

    }

    /**
     * Closes any existing tag then starts a new element. The new element is pushed
     * onto the active element stack.
     **/

    public void begin(String name)
    {
        if (_openTag)
            closeTag();

        push(name);

        _writer.print('<');
        _writer.print(name);

        _openTag = true;
        _emptyTag = false;
    }

    /**
     * Starts an element that will not later be matched with an <code>end()</code>
     * call. This is useful for elements such as &lt;hr;&gt; or &lt;br&gt; that
     * do not need closing tags.
     *
     **/

    public void beginEmpty(String name)
    {
        if (_openTag)
            closeTag();

        _writer.print('<');
        _writer.print(name);

        _openTag = true;
        _emptyTag = true;
    }

    /**
     * Invokes <code>checkError()</code> on the
     *  <code>PrintWriter</code> used to format output.
     **/

    public boolean checkError()
    {
        return _writer.checkError();
    }

    private void checkTagOpen()
    {
        if (!_openTag)
            throw new IllegalStateException(
                Tapestry.getString("AbstractMarkupWriter.tag-not-open"));
    }

    /**
     * Closes this <code>IMarkupWriter</code>. Any active elements are closed. The
     * {@link PrintWriter} is then  sent {@link PrintWriter#close()}.
     *
     **/

    public void close()
    {
        if (_openTag)
            closeTag();

        // Close any active elements.

        while (_depth > 0)
        {
            _writer.print("</");
            _writer.print(pop());
            _writer.print('>');
        }

        _writer.close();

        _writer = null;
        _activeElementStack = null;
        _buffer = null;
    }

    /**
     *  Closes the most recently opened element by writing the '&gt;' that ends
     *  it. May write a slash before the '&gt;' if the tag
     *  was opened by {@link #beginEmpty(String)}.
     * 
     *  <p>Once this is invoked, the <code>attribute()</code> methods
     *  may not be used until a new element is opened with {@link #begin(String)} or
     *  or {@link #beginEmpty(String)}.
     **/

    public void closeTag()
    {
        if (_emptyTag)
            _writer.print('/');

        _writer.print('>');

        _openTag = false;
        _emptyTag = false;
    }

    /**
     * Writes an GTML comment. Any open tag is first closed. 
     * The method takes care of
     * providing the <code>&lt;!--</code> and <code>--&gt;</code>, 
     * including a blank line after the close of the comment.
     *
     * <p>Most characters are valid inside an GTML comment, so no check
     * of the contents is made (much like {@link #printRaw(String)}.
     *
     **/

    public void comment(String value)
    {
        if (_openTag)
            closeTag();

        _writer.print("<!-- ");
        _writer.print(value);
        _writer.println(" -->");
    }

    /**
     * Ends the element most recently started by {@link #begin(String)}. 
     * The name of the tag
     * is popped off of the active element stack and used to form an GTML close tag.
     *
     * <p>TBD: Error checking for the open element stack empty.
     **/

    public void end()
    {
        if (_openTag)
            closeTag();

        _writer.print("</");
        _writer.print(pop());
        _writer.print('>');
    }

    /**
     * Ends the most recently started element with the given name. This will
     * also end any other intermediate elements. This is very useful for easily
     * ending a table or even an entire page.
     *
     * <p>TBD: Error check if the name matches nothing on the open tag stack.
     **/

    public void end(String name)
    {
        if (_openTag)
            closeTag();

        while (true)
        {
            String tagName = pop();

            _writer.print("</");
            _writer.print(tagName);
            _writer.print('>');

            if (tagName.equals(name))
                break;
        }
    }

    /**
     * Forwards <code>flush()</code> to this <code>AbstractMarkupWriter</code>'s 
     * <code>PrintWriter</code>.
     *
     **/

    public void flush()
    {
        _writer.flush();
    }

    /**
     *  Removes the top element from the active element stack and returns it.
     *
     **/

    protected final String pop()
    {
        String result = (String) _activeElementStack.pop();
        _depth--;

        return result;
    }

    /**
     *
     * The primary <code>print()</code> method, used by most other methods.
     *
     * <p>Prints the character array, first closing any open tag. Problematic characters
     * ('&lt;', '&gt;' and '&amp;') are converted to their
     * GTML entities.
     *
     * <p>All 'unsafe' characters are properly converted to either a named
     * or numeric GTML entity.  This can be somewhat expensive, so use
     * {@link #printRaw(char[], int, int)} if the data to print is guarenteed
     * to be safe.
     *
     * <p>Does <em>nothing</em> if <code>data</code> is null.
     *
     * <p>Closes any open tag.
     *
     **/

    public void print(char[] data, int offset, int length)
    {
        if (data == null)
            return;

        if (_openTag)
            closeTag();

        safePrint(data, offset, length, false);
    }

    /**
     * Prints a single character. If the character is not a 'safe' character,
     * such as '&lt;', then it's GTML entity (named or numeric) is printed instead.
     *
     * <p>Closes any open tag.
     *
     **/

    public void print(char value)
    {
        if (_openTag)
            closeTag();

        if (value < _safe.length && _safe[value])
        {
            _writer.print(value);
            return;
        }

        String entity = null;

        if (value < _entities.length)
            entity = _entities[value];

        if (entity != null)
        {
            _writer.print(entity);
            return;
        }

        // Not a well-known entity.  Print it's numeric equivalent.  Note:  this omits
        // the leading '0', but most browsers (IE 5.0) don't seem to mind.  Is this a bug?

        _writer.print("&#" + (int) value + ";");
    }

    /**
     * Prints an integer.
     *
     * <p>Closes any open tag.
     *
     **/

    public void print(int value)
    {
        if (_openTag)
            closeTag();

        _writer.print(value);
    }

    /**
     * Invokes {@link #print(char[], int, int)} to print the string.  Use
     * {@link #printRaw(String)} if the character data is known to be safe.
     *
     * <p>Does <em>nothing</em> if <code>value</code> is null.
     *
     * <p>Closes any open tag.
     *
     * @see #print(char[], int, int)
     *
     **/

    public void print(String value)
    {
        if (value == null)
            return;

        int length = value.length();

        if (_buffer == null || _buffer.length < length)
            _buffer = new char[length];

        value.getChars(0, length, _buffer, 0);

        print(_buffer, 0, length);
    }

    /**
     * Closes the open tag (if any), then prints a line seperator to the output stream.
     *
     **/

    public void println()
    {
        if (_openTag)
            closeTag();

        _writer.println();
    }

    /**
     * Prints and portion of an output buffer to the stream.
     * No escaping of invalid GTML elements is done, which
     * makes this more effecient than <code>print()</code>. 
     * Does <em>nothing</em> if <code>buffer</code>
     * is null.
     *
     * <p>Closes any open tag.
     *
     **/

    public void printRaw(char[] buffer, int offset, int length)
    {
        if (buffer == null)
            return;

        if (_openTag)
            closeTag();

        _writer.write(buffer, offset, length);
    }

    /**
     * Prints output to the stream. No escaping of invalid GTML elements is done, which
     * makes this more effecient than <code>print()</code>. Does <em>nothing</em> 
     * if <code>value</code>
     * is null.
     *
     * <p>Closes any open tag.
     *
     **/

    public void printRaw(String value)
    {
        if (value == null)
            return;

        if (_openTag)
            closeTag();

        _writer.print(value);
    }

    /**
     *  Adds an element to the active element stack.
     *
     **/

    protected final void push(String name)
    {
        if (_activeElementStack == null)
            _activeElementStack = new Stack();

        _activeElementStack.push(name);

        _depth++;
    }

    /**
     * Internal support for safe printing.  Ensures that all characters emitted
     * are safe: either valid GTML characters or GTML entities (named or numeric).
     **/

    private void safePrint(char[] data, int offset, int length, boolean isAttribute)
    {
        int safelength = 0;
        int start = offset;

        for (int i = 0; i < length; i++)
        {
            char ch = data[offset + i];

            // Ignore safe characters.  In an attribute, quotes
            // are not ok and are escaped.

            boolean isSafe = (ch < _safe.length && _safe[ch]);

            if (isAttribute && ch == '"')
                isSafe = false;

            if (isSafe)
            {
                safelength++;
                continue;
            }

            // Write the safe stuff.

            if (safelength > 0)
                _writer.write(data, start, safelength);

            String entity = null;

            // Look for a known entity.

            if (ch < _entities.length)
                entity = _entities[ch];

            // Failing that, emit a numeric entity.

            if (entity == null)
                entity = "&#" + (int) ch + ";";

            _writer.print(entity);

            start = offset + i + 1;
            safelength = 0;
        }

        if (safelength > 0)
            _writer.write(data, start, safelength);
    }

}