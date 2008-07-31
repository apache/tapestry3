//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.util;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentMap;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

/**
 * Manages {@link edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock}s keyed on some arbitrary object.
 */
public class ResourceLockManager
{
    /**
     * Map from some object (which should be a proper key) to a ReentrantLock.
     */
    private final ConcurrentMap resourceToLock = new ConcurrentHashMap();

    public void clear()
    {
        resourceToLock.clear();
    }

    private ReentrantLock get(Object object)
    {
        if (!resourceToLock.containsKey(object))
        {
            // Watch out for a race condition where two threads may put conflicting locks
            // for the name resource.
            resourceToLock.putIfAbsent(object, new ReentrantLock());
        }

        return (ReentrantLock) resourceToLock.get(object);
    }

    public void lock(Object object)
    {
        get(object).lock();
    }

    public void unlock(Object object)
    {
        get(object).unlock();
    }
}
