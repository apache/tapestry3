package net.sf.tapestry.engine;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.Gesture;
import net.sf.tapestry.IComponent;
import net.sf.tapestry.IEngineServiceView;
import net.sf.tapestry.IExternalPage;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.ResponseOutputStream;
import net.sf.tapestry.Tapestry;

/**
 * The external service enables external applications
 * to reference Tapestry pages via a URL. Pages which can be referenced
 * by the external service must implement the {@link IExternalPage}
 * interface. The external service enables the bookmarking of pages.
 * <p> 
 * The URL format for the external service is:
 * <blockquote>
 * <tt>http://localhost/app?service=external&amp;context=[Page Name]&amp;sp=[Param 0]&amp;sp=[Param 1]...</tt>
 * </blockquote>
 * For example to view the "ViewCustomer" page the service parameters 5056 (customer ID) and
 * 309 (company ID) the external service URL would be:
 * <blockquote>
 * <tt>http://localhost/myapp?service=external&amp;context=<b>ViewCustomer</b>&amp;sp=<b>5056</b>&amp;sp=<b>302</b></tt>
 * </blockquote>
 * In this example external service will get a "ViewCustomer" page and invoke the 
 * {@link IExternalPage#activateExternalPage(Object[], IRequestCycle)} method with the parameters:  
 * Object[] { new Integer(5056), new Integer(302) }.
 * <p>
 * Note service parameters (sp) need to be prefixed by valid
 * {@link DataSqueezer} adaptor char. These adaptor chars are automatically provided in 
 * URL's created by the <tt>buildGesture()</tt> method. However if you hand coded an external 
 * service URL you will need to ensure valid prefix chars are present.
 * <p>
 * <table border="1" cellpadding="2">
 *  <tr>
 *   <th>Prefix char(s)</th><th>Mapped Java Type</th>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;TF</td><td>&nbsp;boolean</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;b</td><td>&nbsp;byte</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;c</td><td>&nbsp;char</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;d</td><td>&nbsp;double</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;-0123456789</td><td>&nbsp;integer</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;l</td><td>&nbsp;long</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;S</td><td>&nbsp;String</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;s</td><td>&nbsp;short</td>
 *  </tr>
 *  <tr>
 *   <td>&nbsp;other chars</td>
 *   <td>&nbsp;<tt>String</tt> without truncation of first char</td>
 *  </tr>
 * <table>
 *  <p>
 *  <p>
 *  A good rule of thumb is to keep the information encoded in the URL short and simple, and restrict it
 *  to just Strings and Integers.  Integers can be encoded as-is.  Prefixing all Strings with the letter 'S'
 *  will ensure that they are decoded properly.  Again, this is only relevant if an 
 *  {@link net.sf.tapestry.IExternalPage} is being referenced from static HTML or JSP and the
 *  URL must be assembled in user code ... when the URL is generated by Tapestry, it is automatically
 *  created with the correct prefixes and encodings (as with any other service).
 * 
 * @see net.sf.tapestry.IExternalPage
 *
 * @author Howard Lewis Ship
 * @author Malcolm Edgar
 * @since 2.2
 *  
 **/

public class ExternalService extends AbstractService
{

    public Gesture buildGesture(IRequestCycle cycle, IComponent component, Object[] parameters)
    {
        if (parameters == null || parameters.length == 0)
            throw new ApplicationRuntimeException(Tapestry.getString("service-requires-parameters", EXTERNAL_SERVICE));

        String pageName = (String)parameters[0];
        String[] context = new String[] { pageName };
        
        Object[] pageParameters = new Object[parameters.length - 1];
        System.arraycopy(parameters, 1, pageParameters, 0, parameters.length - 1);

        return assembleGesture(cycle, EXTERNAL_SERVICE, context, pageParameters, true);
    }

    public boolean service(IEngineServiceView engine, IRequestCycle cycle, ResponseOutputStream output)
        throws RequestCycleException, ServletException, IOException
    {
        IExternalPage page = null;

        String[] context = getServiceContext(cycle.getRequestContext());

        if (context == null || context.length != 1)
            throw new ApplicationRuntimeException(
                Tapestry.getString("service-single-context-parameter", EXTERNAL_SERVICE));

        String pageName = context[0];
        
        try
        {
            page = (IExternalPage) cycle.getPage(pageName);
        }
        catch (ClassCastException ex)
        {
            throw new ApplicationRuntimeException(
                Tapestry.getString("ExternalService.page-not-compatible", pageName),
                ex);
        }

        Object[] parameters = getParameters(cycle);

        cycle.setServiceParameters(parameters);
        
        page.validate(cycle);
        cycle.setPage(page);

        page.activateExternalPage(parameters, cycle);

        // Render the response.
        engine.renderResponse(cycle, output);

        return true;
    }

    public String getName()
    {
        return EXTERNAL_SERVICE;
    }
}
