//
// Tapestry Web Application Framework
// Copyright (c) 2000-2002 by Howard Lewis Ship
//
// Howard Lewis Ship
// http://sf.net/projects/tapestry
// mailto:hship@users.sf.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE, as well as License.html. If the license is not
// included with this distribution, you may find a copy at the FSF web
// site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
// Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package net.sf.tapestry.form;

import net.sf.tapestry.IBinding;
import net.sf.tapestry.IForm;
import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.RequiredParameterException;

/**
 *  A component which uses either
 *   &lt;select&gt; and &lt;option&gt; elements 
 *  or &lt;input type=radio&gt; to
 *  set a property of some object.  Typically, the values for the object
 *  are defined using an {@link Enum}.  A PropertySelection is dependent on
 *  an {@link IPropertySelectionModel} to provide the list of possible values.
 *
 *  <p>Often, this is used to select a particular {@link Enum} to assign to a property; the
 * {@link EnumPropertySelectionModel} class simplifies this.
 *
 *  <p>
 *
 * <table border=1>
 * <tr> 
 *    <td>Parameter</td>
 *    <td>Type</td>
 *	  <td>Read / Write </td>
 *    <td>Required</td> 
 *    <td>Default</td>
 *    <td>Description</td>
 * </tr>
 *
 * <tr>
 *		<td>value</td>
 *		<td>java.lang.Object</td>
 *		<td>R / W</td>
 *		<td>yes</td>
 *		<td>&nbsp;</td>
 *		<td>The property to set.  During rendering, this property is read, and sets
 * the default value of the selection (if it is null, no element is selected).
 * When the form is submitted, this property is updated based on the new
 * selection. </td> </tr>
 *
 * <tr>
 *		<td>renderer</td>
 *		<td>{@link IPropertySelectionRenderer}</td>
 *		<td>R</td>
 *		<td>no</td>
 *		<td>shared instance of {@link SelectPropertySelectionRenderer}</td>
 *		<td>Defines the object used to render the PropertySelection.
 * <p>{@link SelectPropertySelectionRenderer} renders the component as a &lt;select&gt;.
 * <p>{@link RadioPropertySelectionRenderer} renders the component as a table of
 * radio buttons.</td></tr>
 *
 *  <tr>
 *		<td>model</td>
 *		<td>{@link IPropertySelectionModel}</td>
 *		<td>R</td>
 *		<td>yes</td>
 *		<td>&nbsp;</td>
 *		<td>The model provides a list of possible labels, and matches those labels
 *  against possible values that can be assigned back to the property.</td> </tr>
 * 
 *  <tr>
 * 		<td>disabled</td>
 *		<td>boolean</td>
 *		<td>R</td>
 *		<td>no</td>
 *		<td>false</td>
 *		<td>Controls whether the &lt;select&gt; is active or not. A disabled PropertySelection
 * does not update its value parameter.
 *			
 *			<p>Corresponds to the <code>disabled</code> HTML attribute.</td>
 *	</tr>
 *
 *	</table>
 *
 * <p>Informal parameters are not allowed,  A body is not allowed.
 *
 *
 *  @version $Id$
 *  @author Howard Lewis Ship
 *
 **/

public class PropertySelection extends AbstractFormComponent
{
    private IBinding valueBinding;
    private IBinding modelBinding;
    private IBinding disabledBinding;
    private IBinding rendererBinding;
    private String name;
    private boolean disabled;

    /**
     *  A shared instance of {@link SelectPropertySelectionRenderer}.
     *
     **/

    public static final IPropertySelectionRenderer DEFAULT_SELECT_RENDERER =
        new SelectPropertySelectionRenderer();

    /**
     *  A shared instance of {@link RadioPropertySelectionRenderer}.
     *
     **/

    public static final IPropertySelectionRenderer DEFAULT_RADIO_RENDERER =
        new RadioPropertySelectionRenderer();

    public IBinding getValueBinding()
    {
        return valueBinding;
    }

    public void setValueBinding(IBinding value)
    {
        valueBinding = value;
    }

    public IBinding getModelBinding()
    {
        return modelBinding;
    }

    public void setModelBinding(IBinding value)
    {
        modelBinding = value;
    }

    public IBinding getDisabledBinding()
    {
        return disabledBinding;
    }

    public void setDisabledBinding(IBinding value)
    {
        disabledBinding = value;
    }

    public void setRendererBinding(IBinding value)
    {
        rendererBinding = value;
    }

    public IBinding getRendererBinding()
    {
        return rendererBinding;
    }

    /**
     *  Returns the name assigned to this PropertySelection by the {@link Form}
     *  that wraps it.
     *
     **/

    public String getName()
    {
        return name;
    }

    /**
     *  Returns true if this PropertySelection's disabled parameter yields true.
     *  The corresponding HTML control(s) should be disabled.
     **/

    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     *  Renders the component, much of which is the responsiblity
     *  of the {@link IPropertySelectionRenderer renderer}.  The possible options,
     *  thier labels, and the values to be encoded in the form are provided
     *  by the {@link IPropertySelectionModel model}.
     *
     **/

    public void render(IMarkupWriter writer, IRequestCycle cycle) throws RequestCycleException
    {
        IPropertySelectionRenderer renderer = null;
        Object newValue;
        Object currentValue;
        Object option;
        String optionValue;
        int i;
        boolean selected = false;
        boolean foundSelected = false;
        int count;
        boolean radio = false;

        IForm form = getForm(cycle);

        boolean rewinding = form.isRewinding();

        if (disabledBinding == null)
            disabled = false;
        else
            disabled = disabledBinding.getBoolean();

        IPropertySelectionModel model =
            (IPropertySelectionModel) modelBinding.getObject("model", IPropertySelectionModel.class);

        if (model == null)
            throw new RequiredParameterException(this, "model", modelBinding);

        name = form.getElementId(this);

        if (rewinding)
        {
            // If disabled, ignore anything that comes up from the client.

            if (disabled)
                return;

            optionValue = cycle.getRequestContext().getParameter(name);

            if (optionValue == null)
                newValue = null;
            else
                newValue = model.translateValue(optionValue);

            valueBinding.setObject(newValue);

            return;
        }

        if (rendererBinding != null)
            renderer =
                (IPropertySelectionRenderer) rendererBinding.getObject(
                    "renderer",
                    IPropertySelectionRenderer.class);

        if (renderer == null)
            renderer = DEFAULT_SELECT_RENDERER;

        renderer.beginRender(this, writer, cycle);

        count = model.getOptionCount();
        currentValue = valueBinding.getObject();

        for (i = 0; i < count; i++)
        {
            option = model.getOption(i);

            if (!foundSelected)
            {
                selected = isEqual(option, currentValue);
                if (selected)
                    foundSelected = true;
            }

            renderer.renderOption(this, writer, cycle, model, option, i, selected);

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
}