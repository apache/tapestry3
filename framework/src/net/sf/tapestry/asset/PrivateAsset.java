package net.sf.tapestry.asset;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.Gesture;
import net.sf.tapestry.IAsset;
import net.sf.tapestry.IEngineService;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.Tapestry;

/**
 *  An implementation of {@link IAsset} for localizable assets within
 *  the JVM's classpath.
 *
 *  <p>The localization code here is largely cut-and-paste from
 *  {@link ContextAsset}.
 *
 *  @author Howard Ship
 *  @version $Id$
 * 
 **/

public class PrivateAsset implements IAsset
{
    private static final Log LOG = LogFactory.getLog(PrivateAsset.class);

    private AssetExternalizer _externalizer;

    private String _resourcePath;

    /**
     *  Map, keyed on Locale, value is the localized resourcePath (as a String)
     **/

    private Map _localizations;

    public PrivateAsset(String resourcePath)
    {
        _resourcePath = resourcePath;
    }

    /**
     *  Gets the localized version of the resource.  Build
     *  the URL for the resource.  If possible, the application's
     *  {@link AssetExternalizer} is located, to copy the resource to
     *  a directory visible to the web server.
     *
     **/

    public String buildURL(IRequestCycle cycle)
    {
        String localizedResourcePath = findLocalization(cycle, cycle.getPage().getLocale());

        if (_externalizer == null)
            _externalizer = AssetExternalizer.get(cycle);

        String externalURL = _externalizer.getURL(localizedResourcePath);

        if (externalURL != null)
            return externalURL;

        // Otherwise, the service is responsible for dynamically retrieving the
        // resource.

        String[] parameters = new String[] { localizedResourcePath };

        IEngineService service = cycle.getEngine().getService(IEngineService.ASSET_SERVICE);

        Gesture g = service.buildGesture(cycle, null, parameters);

        return g.getURL();
    }

    public InputStream getResourceAsStream(IRequestCycle cycle)
    {
        return getResourceAsStream(cycle, cycle.getPage().getLocale());
    }

    public InputStream getResourceAsStream(IRequestCycle cycle, Locale locale)
    {
        try
        {
            IResourceResolver resolver = cycle.getEngine().getResourceResolver();

            URL url = resolver.getResource(findLocalization(cycle, locale));

            return url.openStream();
        }
        catch (Exception ex)
        {
            throw new ApplicationRuntimeException(
                Tapestry.getString("PrivateAsset.resource-missing", _resourcePath),
                ex);
        }
    }

    /**
     *  Poke around until we find the localized version of the asset.
     *
     *  <p>A lot of this is cut-and-paste from DefaultTemplateSource.  I haven't
     *  come up with a good, general, efficient way to do this search without
     *  a huge amount of mechanism.
     *
     **/

    private synchronized String findLocalization(IRequestCycle cycle, Locale locale)
    {
        if (_localizations == null)
            _localizations = new HashMap();

        String result = (String) _localizations.get(locale);
        if (result != null)
            return result;

        if (LOG.isDebugEnabled())
            LOG.debug(
                "Searching for localization of private asset "
                    + _resourcePath
                    + " in locale "
                    + locale.getDisplayName());

        int dotx = _resourcePath.lastIndexOf('.');
        String suffix = _resourcePath.substring(dotx);

        StringBuffer buffer = new StringBuffer(dotx + 30);

        buffer.append(_resourcePath.substring(0, dotx));
        int rawLength = buffer.length();

        int start = 2;

        String country = locale.getCountry();
        if (country.length() > 0)
            start--;

        // This assumes that you never have the case where there's
        // a null language code and a non-null country code.

        String language = locale.getLanguage();
        if (language.length() > 0)
            start--;

        IResourceResolver resolver = cycle.getEngine().getResourceResolver();

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

            buffer.append(suffix);

            String candidatePath = buffer.toString();

            if (resolver.getResource(candidatePath) != null)
            {
                _localizations.put(locale, candidatePath);

                if (LOG.isDebugEnabled())
                    LOG.debug("Found " + candidatePath);

                return candidatePath;
            }

        }

        throw new ApplicationRuntimeException(
            Tapestry.getString("PrivateAsset.resource-unavailable", _resourcePath, locale));
    }

    public String toString()
    {
        return "PrivateAsset[" + _resourcePath + "]";
    }

}