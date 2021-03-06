//  Copyright 2004, 2008 The Apache Software Foundation
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

package org.apache.tapestry.engine;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.*;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.resource.ClasspathResourceLocation;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.apache.tapestry.spec.LibrarySpecification;
import org.apache.tapestry.util.IRenderDescription;
import org.apache.tapestry.util.ResourceLockManager;
import org.apache.tapestry.util.pool.Pool;
import org.apache.tapestry.util.xml.DocumentParseException;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link ISpecificationSource} that expects to use the normal class loader to locate
 * component specifications from within the classpath.
 * <p/>
 * <p>Caches specifications in memory forever, or until {@link #reset()} is invoked.
 * <p/>
 * <p>An instance of this class acts like a singleton and is shared by multiple sessions, so it must be threadsafe.
 *
 * @author Howard Lewis Ship
 * @version $Id$
 */

public class DefaultSpecificationSource implements ISpecificationSource, IRenderDescription
{
    private static final Log LOG = LogFactory.getLog(DefaultSpecificationSource.class);

    /**
     * Key used to get and store {@link SpecificationParser} instances from the Pool.
     *
     * @since 3.0
     */

    private static final String PARSER_POOL_KEY = "org.apache.tapestry.SpecificationParser";

    private IResourceResolver _resolver;
    private IApplicationSpecification _specification;

    private INamespace _applicationNamespace;
    private INamespace _frameworkNamespace;

    /**
     * Contains previously parsed component specifications.
     */

    private final Map _componentCache = new ConcurrentHashMap();

    /**
     * Contains previously parsed page specifications.
     *
     * @since 2.2
     */

    private final Map _pageCache = new ConcurrentHashMap();

    /**
     * Contains previously parsed library specifications, keyed on specification resource path.
     *
     * @since 2.2
     */

    private final Map _libraryCache = new ConcurrentHashMap();


    /**
     * Used to synchronize concurrent operations on specific resources.
     */
    private final ResourceLockManager _resourceLockManager = new ResourceLockManager();

    /**
     * Reference to the shared {@link org.apache.tapestry.util.pool.Pool}.
     *
     * @see org.apache.tapestry.IEngine#getPool()
     * @since 3.0
     */

    private Pool _pool;

    public DefaultSpecificationSource(
            IResourceResolver resolver,
            IApplicationSpecification specification,
            Pool pool)
    {
        _resolver = resolver;
        _specification = specification;
        _pool = pool;
    }

    /**
     * Clears the specification cache.  This is used during debugging.
     */

    public synchronized void reset()
    {
        _componentCache.clear();
        _pageCache.clear();
        _libraryCache.clear();
        _resourceLockManager.clear();

        _applicationNamespace = null;
        _frameworkNamespace = null;
    }

    protected IComponentSpecification parseSpecification(
            IResourceLocation resourceLocation,
            boolean asPage)
    {
        IComponentSpecification result = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing component specification " + resourceLocation);

        SpecificationParser parser = getParser();

        try
        {
            if (asPage)
                result = parser.parsePageSpecification(resourceLocation);
            else
                result = parser.parseComponentSpecification(resourceLocation);
        }
        catch (DocumentParseException ex)
        {
            throw new ApplicationRuntimeException(
                    Tapestry.format(
                            "DefaultSpecificationSource.unable-to-parse-specification",
                            resourceLocation),
                    ex);
        }
        finally
        {
            discardParser(parser);
        }

        return result;
    }

    protected ILibrarySpecification parseLibrarySpecification(IResourceLocation resourceLocation)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("Parsing library specification " + resourceLocation);

        try
        {
            return getParser().parseLibrarySpecification(resourceLocation);
        }
        catch (DocumentParseException ex)
        {
            throw new ApplicationRuntimeException(
                    Tapestry.format(
                            "DefaultSpecificationSource.unable-to-parse-specification",
                            resourceLocation),
                    ex);
        }

    }

    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this);

        builder.append("applicationNamespace", _applicationNamespace);
        builder.append("frameworkNamespace", _frameworkNamespace);
        builder.append("specification", _specification);

        return builder.toString();
    }

    /**
     * @since 1.0.6 *
     */

    public void renderDescription(IMarkupWriter writer)
    {
        writer.print("DefaultSpecificationSource[");

        writeCacheDescription(writer, "page", _pageCache);
        writer.beginEmpty("br");
        writer.println();

        writeCacheDescription(writer, "component", _componentCache);
        writer.print("]");
        writer.println();
    }

    private void writeCacheDescription(IMarkupWriter writer, String name, Map cache)
    {
        Set keySet = cache.keySet();

        writer.print(Tapestry.size(keySet));
        writer.print(" cached ");
        writer.print(name);
        writer.print(" specifications:");

        boolean first = true;

        Iterator i = keySet.iterator();
        while (i.hasNext())
        {
            // The keys are now IResourceLocation instances

            Object key = i.next();

            if (first)
            {
                writer.begin("ul");
                first = false;
            }

            writer.begin("li");
            writer.print(key.toString());
            writer.end();
        }

        if (!first)
            writer.end(); // <ul>
    }

    /**
     * Gets a component specification.
     *
     * @param resourceLocation the complete resource path to the specification.
     * @throws ApplicationRuntimeException if the specification cannot be obtained.
     */

    public IComponentSpecification getComponentSpecification(IResourceLocation resourceLocation)
    {
        IComponentSpecification result =
                (IComponentSpecification) _componentCache.get(resourceLocation);

        if (result != null)
            return result;

        _resourceLockManager.lock(resourceLocation);

        try
        {
            result = (IComponentSpecification) _componentCache.get(resourceLocation);

            if (result != null)
                return result;

            result = parseSpecification(resourceLocation, false);

            _componentCache.put(resourceLocation, result);
        }
        finally
        {
            _resourceLockManager.unlock(resourceLocation);

        }

        return result;
    }

    public IComponentSpecification getPageSpecification(IResourceLocation resourceLocation)
    {
        IComponentSpecification result = (IComponentSpecification) _pageCache.get(resourceLocation);

        if (result != null)
            return result;

        _resourceLockManager.lock(resourceLocation);

        try
        {
            // In a race condition, one thread may be parsing the specification while others block.
            // The specification is in the cache once they un-block.

            result = (IComponentSpecification) _pageCache.get(resourceLocation);


            if (result != null)
                return result;

            result = parseSpecification(resourceLocation, true);

            _pageCache.put(resourceLocation, result);
        }
        finally
        {
            _resourceLockManager.unlock(resourceLocation);

        }

        return result;
    }

    public ILibrarySpecification getLibrarySpecification(IResourceLocation resourceLocation)
    {
        ILibrarySpecification result = (LibrarySpecification) _libraryCache.get(resourceLocation);

        if (result != null)
            return result;

        _resourceLockManager.lock(resourceLocation);

        try
        {
            // In a race condition, one thread may be parsing the specification while others block.
            // The specification is in the cache once they un-block.

            result = (LibrarySpecification) _libraryCache.get(resourceLocation);

            if (result != null)
                return result;

            result = parseLibrarySpecification(resourceLocation);
            _libraryCache.put(resourceLocation, result);
        }
        finally
        {
            _resourceLockManager.unlock(resourceLocation);

        }

        return result;
    }

    /**
     * @since 2.2 *
     */

    protected SpecificationParser getParser()
    {
        SpecificationParser result = (SpecificationParser) _pool.retrieve(PARSER_POOL_KEY);

        if (result == null)
            result = new SpecificationParser(_resolver);

        return result;
    }

    /**
     * @since 3.0 *
     */

    protected void discardParser(SpecificationParser parser)
    {
        _pool.store(PARSER_POOL_KEY, parser);
    }

    public synchronized INamespace getApplicationNamespace()
    {
        if (_applicationNamespace == null)
            _applicationNamespace = new Namespace(null, null, _specification, this);

        return _applicationNamespace;
    }

    public synchronized INamespace getFrameworkNamespace()
    {
        if (_frameworkNamespace == null)
        {
            IResourceLocation frameworkLocation =
                    new ClasspathResourceLocation(_resolver, "/org/apache/tapestry/Framework.library");

            ILibrarySpecification ls = getLibrarySpecification(frameworkLocation);

            _frameworkNamespace = new Namespace(INamespace.FRAMEWORK_NAMESPACE, null, ls, this);
        }

        return _frameworkNamespace;
    }
}