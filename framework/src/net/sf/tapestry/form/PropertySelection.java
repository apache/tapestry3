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
package net.sf.tapestry.form;

import net.sf.tapestry.IForm;
import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.Tapestry;

/**
 *  A component used to render a drop-down list of options that
 *  the user may select.
 * 
 *  [<a href="../../../../../ComponentReference/PropertySelection.html">Component Reference</a>]
 *
 *  <p>Earlier versions of PropertySelection (through release 2.2)
 *  were more flexible, they included a <b>renderer</b> property
 *  that controlled how the selection was rendered.  Ultimately,
 *  this proved of little value and this portion of
 *  functionality was deprecated in 2.3 and will be removed in 2.3.
 * 
 *  <p>Typically, the values available to be selected
 *  are defined using an {@link net.sf.tapestry.util.Enum}.  
 *  A PropertySelection is dependent on
 *  an {@link IPropertySelectionModel} to provide the list of possible values.
 *
 *  <p>Often, this is used to select a particular 
 *  {@link net.sf.tapestry.util.Enum} to assign to a property; the
 *  {@link EnumPropertySelectionModel} class simplifies this.
 *
 *
 *  @version $Id$
 *  @author Howard Lewis Ship
 *
 **/

public class PropertySelection extends AbstractFormComponent
{
    /** @since 2.2 **/
    private Object _value;
    private IPropertySelectionRenderer _renderer;
    private IPropertySelectionModel _model;
    private boolean _disabled;

    private String _name;
    private boolean _submitOnChange;

    /**
     *  A shared instance of {@link SelectPropertySelectionRenderer}.
     *
     * 
     **/

    public static final IPropertySelectionRenderer DEFAULT_SELECT_RENDERER = new SelectPropertySelectionRenderer();

    /**
     *  A shared instance of {@link RadioPropertySelectionRenderer}.
     *
     * 
     **/

    public static final IPropertySelectionRenderer DEFAULT_RADIO_RENDERER = new RadioPropertySelectionRenderer();


    /**
     *  Returns the name assigned to this PropertySelection by the {@link Form}
     *  that wraps it.
     *
     **/

    public String getName()
    {
        return _name;
    }

    /**
     *  Returns true if this PropertySelection's disabled parameter yields true.
     *  The corresponding HTML control(s) should be disabled.
     **/

    public boolean isDisabled()
    {
        return _disabled;
    }

    /**
     *  Renders the component, much of which is the responsiblity
     *  of the {@link IPropertySelectionRenderer renderer}.  The possible options,
     *  thier labels, and the values to be encoded in the form are provided
     *  by the {@link IPropertySelectionModel model}.
     *
     **/

    protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle) throws RequestCycleException
    {
        IForm form = getForm(cycle);
        if (form == null)
            throw new RequestCycleException(Tapestry.getString("must-be-wrapped-by-form", "PropertySelection"), this);

        boolean rewinding = form.isRewinding();

        _name = form.getElementId(this);

        if (rewinding)
        {
            // If disabled, ignore anything that comes up from the client.

            if (_disabled)
                return;

            String optionValue = cycle.getRequestContext().getParameter(_name);

          _value = (optionValue == null) ? null : _model.translateValue(optionValue);

            return;
        }

        // Support for 2.2 style renderer.  This goes in 2.3.

        if (_renderer != null)
        {
            renderWithRenderer(writer, cycle, _renderer);
            return;
        }

        writer.begin("select");
        writer.attribute("name", _name);

        if (_disabled)
            writer.attribute("disabled");

        if (_submitOnChange)
            writer.attribute("onchange", "javascript:this.form.submit();");

        // Apply informal attributes.

        generateAttributes(writer, cycle);

        writer.println();

        int count = _model.getOptionCount();
        boolean foundSelected = false;
        boolean selected = false;

        for (int i = 0; i < count; i++)
        {
            Object option = _model.getOption(i);

            if (!foundSelected)
            {
                selected = isEqual(option, _value);
                if (selected)
                    foundSelected = true;
            }

            writer.begin("option");
            writer.attribute("value", _model.getValue(i));

            if (selected)
                writer.attribute("selected");

            writer.print(_model.getLabel(i));

            writer.end();

            writer.println();

            selected = false;
        }

        writer.end(); // <select>

    }

    /**
     *  Renders the property selection using a {@link IPropertySelectionRenderer}.
     *  Support for this will be removed in 2.3.
     * 
     **/

    private void renderWithRenderer(IMarkupWriter writer, IRequestCycle cycle, IPropertySelectionRenderer renderer)
        throws RequestCycleException
    {
        renderer.beginRender(this, writer, cycle);

        int count = _model.getOptionCount();

        boolean foundSelected = false;
        boolean selected = false;

        for (int i = 0; i < count; i++)
        {
            Object option = _model.getOption(i);

            if (!foundSelected)
            {
                selected = isEqual(option, _value);
                if (selected)
                    foundSelected = true;
            }

            renderer.renderOption(this, writer, cycle, _model, option, i, selected);

            selected = false;
        }

        // A PropertySelection doesn't allow a body, so no need to worry about
        // wrapped components.

        renderer.endRender(this, writer, cycle);
    }

    private boolean isEqual(Object left, Object right)
    {
        // Both null, or same object, then are equal

        if (left == right)
            return true;

        // If one is null, the other isn't, then not equal.

        if (left == null || right == null)
            return false;

        // Both non-null; use standard comparison.

        return left.equals(right);
    }

    public void setDisabled(boolean disabled)
    {
        _disabled = disabled;
    }

    public IPropertySelectionModel getModel()
    {
        return _model;
    }

    public void setModel(IPropertySelectionModel model)
    {
        _model = model;
    }

    public IPropertySelectionRenderer getRenderer()
    {
        return _renderer;
    }

    public void setRenderer(IPropertySelectionRenderer renderer)
    {
        _renderer = renderer;
    }

    /** @since 2.2 **/

    public boolean getSubmitOnChange()
    {
        return _submitOnChange;
    }

    /** @since 2.2 **/

    public void setSubmitOnChange(boolean submitOnChange)
    {
        _submitOnChange = submitOnChange;
    }

    /** @since 2.2 **/
    
    public Object getValue()
    {
        return _value;
    }

    /** @since 2.2 **/
    
    public void setValue(Object value)
    {
        _value = value;
    }

}