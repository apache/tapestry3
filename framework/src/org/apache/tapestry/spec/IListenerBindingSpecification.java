/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package org.apache.tapestry.spec;

/**
 *  Special interface of {@link org.apache.tapestry.spec.IBindingSpecification} used
 *  to encapsulate additional information the additional information 
 *  specific to listener bindings.  In a IListenerBindingSpecification, the
 *  value property is the actual script (and is aliased as property script), 
 *  but an additional property,
 *  language, (which may be null) is needed.  This is the language
 *  the script is written in. * 
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 * @since 3.0
 */
public interface IListenerBindingSpecification extends IBindingSpecification
{
    public abstract String getLanguage();
    public abstract String getScript();
    public abstract void setLanguage(String language);
}