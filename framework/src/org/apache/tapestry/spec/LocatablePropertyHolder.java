//  Copyright 2004 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.spec;

import org.apache.tapestry.ILocation;
import org.apache.tapestry.ILocationHolder;
import org.apache.tapestry.util.BasePropertyHolder;

/**
 *  Base class for implementing both
 *  interfaces {@link org.apache.tapestry.util.IPropertyHolder} and
 *  {@link org.apache.tapestry.ILocationHolder}.  This is
 *  used by all the specification classes.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 3.0
 *
 **/

public class LocatablePropertyHolder extends BasePropertyHolder implements ILocationHolder
{
	private ILocation _location;
	
    public ILocation getLocation()
    {
        return _location;
    }

    public void setLocation(ILocation location)
    {
        _location = location;
    }

}
