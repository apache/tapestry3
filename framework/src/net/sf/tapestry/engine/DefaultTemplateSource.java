package net.sf.tapestry.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.IAsset;
import net.sf.tapestry.IComponent;
import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRenderDescription;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.ITemplateSource;
import net.sf.tapestry.NoSuchComponentException;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.parse.ComponentTemplate;
import net.sf.tapestry.parse.ITemplateParserDelegate;
import net.sf.tapestry.parse.TemplateParseException;
import net.sf.tapestry.parse.TemplateParser;
import net.sf.tapestry.parse.TemplateToken;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.util.MultiKey;

/**
 *  Default implementation of {@link ITemplateSource}.  Templates, once parsed,
 *  stay in memory until explicitly cleared.
 *
 *  <p>An instance of this class acts as a singleton shared by all sessions, so it
 *  must be threadsafe.
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 * 
 **/

public class DefaultTemplateSource implements ITemplateSource, IRenderDescription
{
    private static final Log LOG = LogFactory.getLog(DefaultTemplateSource.class);

    // Cache of previously retrieved templates.  Key is a multi-key of 
    // specification resource path and locale (local may be null), value
    // is the ComponentTemplate.

    private Map _cache = new HashMap();

    // Previously read templates; key is the HTML resource path, value
    // is the ComponentTemplate.

    private Map _templates = new HashMap();

    /**
     *  Number of tokens (each template contains multiple tokens).
     *
     **/

    private int _tokenCount;

    private static final int BUFFER_SIZE = 2000;

    private IResourceResolver _resolver;
    private TemplateParser _parser;

    private static class ParserDelegate implements ITemplateParserDelegate
    {
        IComponent _component;

        ParserDelegate(IComponent component)
        {
            _component = component;
        }

        public boolean getKnownComponent(String componentId)
        {
            try
            {
                _component.getComponent(componentId);

                return true;
            }
            catch (NoSuchComponentException ex)
            {
                return false;
            }
        }

        public boolean getAllowBody(String componentId)
        {
            return _component.getComponent(componentId).getSpecification().getAllowBody();
        }
    }

    public DefaultTemplateSource(IResourceResolver resolver)
    {
        _resolver = resolver;
    }

    /**
     *  Clears the template cache.  This is used during debugging.
     *
     **/

    public void reset()
    {
        _cache = null;
        _templates.clear();

        _tokenCount = 0;
    }

    /**
     *  Reads the template for the component.
     *
     *  <p>Returns null if the template can't be found.
     **/

    public ComponentTemplate getTemplate(IRequestCycle cycle, IComponent component)
    {
        ComponentSpecification specification = component.getSpecification();
        String specificationResourcePath = specification.getSpecificationResourcePath();
        Locale locale = component.getPage().getLocale();

        Object key = new MultiKey(new Object[] { specificationResourcePath, locale }, false);

        ComponentTemplate result = searchCache(key);
        if (result != null)
            return result;

        result = findTemplate(cycle, specificationResourcePath, component, locale);

        if (result == null)
        {
            String stringKey =
                (locale == null) ? "DefaultTemplateSource.no-template" : "DefaultTemplateSource.no-template-in-locale";

            throw new ApplicationRuntimeException(Tapestry.getString(stringKey, component.getExtendedId(), locale));
        }

        saveToCache(key, result);

        return result;
    }

    private synchronized ComponentTemplate searchCache(Object key)
    {
        if (_cache == null)
            return null;

        return (ComponentTemplate) _cache.get(key);

    }

    private synchronized void saveToCache(Object key, ComponentTemplate template)
    {
        if (_cache == null)
            _cache = new HashMap();

        _cache.put(key, template);

    }

    private synchronized ComponentTemplate findTemplate(
        IRequestCycle cycle,
        String specificationResourcePath,
        IComponent component,
        Locale locale)
    {
        IAsset templateAsset = component.getAsset(TEMPLATE_ASSET_NAME);

        if (templateAsset != null)
            return readTemplateFromAsset(cycle, component, templateAsset, locale);

        return findStandardTemplate(specificationResourcePath, component, locale);
    }

    /**
     *  Reads an asset to get the template.
     * 
     **/

    private synchronized ComponentTemplate readTemplateFromAsset(IRequestCycle cycle, IComponent component, IAsset asset, Locale locale)
    {
        InputStream stream = asset.getResourceAsStream(cycle, locale);

        char[] templateData = null;

        try
        {

            templateData = readTemplateStream(stream);

            stream.close();
        }
        catch (IOException ex)
        {
            throw new ApplicationRuntimeException(
                Tapestry.getString("DefaultTemplateSource.unable-to-read-template", asset),
                ex);
        }

        return constructTokens(templateData, asset.toString(), component);
    }

    /**
     *  Search for the template corresponding to the resource and the locale.
     *  This may be in the template map already, or may involve reading and
     *  parsing the template.
     *
     **/

    private synchronized ComponentTemplate findStandardTemplate(
        String specificationResourcePath,
        IComponent component,
        Locale locale)
    {
        String candidatePath = null;
        String language = null;
        String country = null;

        if (LOG.isDebugEnabled())
            LOG.debug(
                "Searching for localized version of template for "
                    + specificationResourcePath
                    + " in locale "
                    + locale.getDisplayName());

        int dotx = specificationResourcePath.lastIndexOf('.');
        StringBuffer buffer = new StringBuffer(dotx + 20);
        buffer.append(specificationResourcePath.substring(0, dotx));
        int rawLength = buffer.length();
        int start = 2;

        if (locale != null)
        {
            country = locale.getCountry();
            if (country.length() > 0)
                start--;

            // This assumes that you never have the case where there's
            // a null language code and a non-null country code.

            language = locale.getLanguage();
            if (language.length() > 0)
                start--;
        }

        ComponentTemplate result = null;

        // On pass #0, we use language code and country code
        // On pass #1, we use language code
        // On pass #2, we use neither.
        // We skip pass #0 or #1 depending on whether the language code
        // and/or country code is null.

        for (int i = start; i < 3; i++)
        {
            buffer.setLength(rawLength);

            if (i < 2)
            {
                buffer.append('_');
                buffer.append(language);
            }

            if (i == 0)
            {
                buffer.append('_');
                buffer.append(country);
            }

            buffer.append(".html");

            candidatePath = buffer.toString();

            // See if it's been parsed before

            result = (ComponentTemplate) _templates.get(candidatePath);
            if (result != null)
                break;

            // Ok, see if it exists.

            result = parseTemplate(candidatePath, component);

            if (result != null)
            {
                _templates.put(candidatePath, result);
                break;
            }
        }

        return result;
    }

    /**
     *  Reads the template for the given resource; returns null if the
     *  resource doesn't exist.  Note that this method is only invoked
     *  from a synchronized block, so there shouldn't be threading
     *  issues here.
     *
     **/

    private ComponentTemplate parseTemplate(String resourceName, IComponent component)
    {
        char[] templateData = readTemplate(resourceName);
        if (templateData == null)
            return null;

        return constructTokens(templateData, resourceName, component);
    }

    private ComponentTemplate constructTokens(char[] templateData, String resourceName, IComponent component)
    {
        if (_parser == null)
            _parser = new TemplateParser();

        ITemplateParserDelegate delegate = new ParserDelegate(component);

        TemplateToken[] tokens;

        try
        {
            tokens = _parser.parse(templateData, delegate, resourceName);
        }
        catch (TemplateParseException ex)
        {
            throw new ApplicationRuntimeException(
                Tapestry.getString("DefaultTemplateSource.unable-to-parse-template", resourceName),
                ex);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Parsed " + tokens.length + " tokens from template");

        _tokenCount += tokens.length;

        return new ComponentTemplate(templateData, tokens);
    }

    /**
     *  Reads the template, given the complete path to the
     *  resource.  Returns null if the resource doesn't exist.
     *
     **/

    private char[] readTemplate(String resourceName)
    {
        URL url;
        InputStream stream = null;

        url = _resolver.getResource(resourceName);

        if (url == null)
            return null;

        if (LOG.isDebugEnabled())
            LOG.debug("Reading template " + resourceName + " from " + url);

        try
        {
            stream = url.openStream();

            return readTemplateStream(stream);
        }
        catch (IOException ex)
        {
            throw new ApplicationRuntimeException(
                Tapestry.getString("DefaultTemplateSource.unable-to-read-template", resourceName),
                ex);
        }
        finally
        {
            try
            {
                if (stream != null)
                    stream.close();
            }
            catch (IOException e)
            {
                // Ignore it!
            }
        }

    }

    /**
     *  Reads a Stream into memory as an array of characters.
     *
     **/

    private char[] readTemplateStream(InputStream stream) throws IOException
    {
        char[] charBuffer = new char[BUFFER_SIZE];
        StringBuffer buffer = new StringBuffer();

        InputStreamReader reader = new InputStreamReader(stream);

        try
        {
            while (true)
            {
                int charsRead = reader.read(charBuffer, 0, BUFFER_SIZE);

                if (charsRead <= 0)
                    break;

                buffer.append(charBuffer, 0, charsRead);
            }
        }
        finally
        {
            reader.close();
        }

        // OK, now reuse the charBuffer variable to
        // produce the final result.

        int length = buffer.length();

        charBuffer = new char[length];

        // Copy the character out of the StringBuffer and into the
        // array.

        buffer.getChars(0, length, charBuffer, 0);

        return charBuffer;
    }

    public synchronized String toString()
    {
        StringBuffer buffer = new StringBuffer("DefaultTemplateSource@");
        buffer.append(Integer.toHexString(hashCode()));

        buffer.append('[');

        if (_cache != null)
            buffer.append(_cache.keySet());

        if (_tokenCount > 0)
        {
            buffer.append(", ");
            buffer.append(_tokenCount);
            buffer.append(" tokens");
        }

        buffer.append(']');

        return buffer.toString();
    }

    /** @since 1.0.6 **/

    public synchronized void renderDescription(IMarkupWriter writer)
    {
        writer.print("DefaultTemplateSource[");

        if (_tokenCount > 0)
        {
            writer.print(_tokenCount);
            writer.print(" tokens");
        }

        if (_cache != null)
        {
            boolean first = true;
            Iterator i = _cache.entrySet().iterator();

            while (i.hasNext())
            {
                if (first)
                {
                    writer.begin("ul");
                    first = false;
                }

                Map.Entry e = (Map.Entry) i.next();
                Object key = e.getKey();
                ComponentTemplate template = (ComponentTemplate) e.getValue();

                writer.begin("li");
                writer.print(key.toString());
                writer.print(" (");
                writer.print(template.getTokenCount());
                writer.print(" tokens)");
                writer.println();
                writer.end();
            }

            if (!first)
            {
                writer.end(); // <ul>
                writer.beginEmpty("br");
            }
        }

        writer.print("]");

    }
}