package tutorial.workbench.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jrefinery.chart.ChartFactory;
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.data.DefaultPieDataset;

import net.sf.tapestry.IAsset;
import net.sf.tapestry.IMarkupWriter;
import net.sf.tapestry.IRequestCycle;
import net.sf.tapestry.RequestCycleException;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.html.BasePage;
import net.sf.tapestry.valid.IValidationDelegate;

/**
 *  Demonstrates more complex form handling (including loops and dynamic addition/deletion of
 *  rows) as well as dynamic image generation using {@link JFreeChart}.
 * 
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 1.0.10
 * 
 **/

public class Chart extends BasePage implements IChartProvider
{
    private List plotValues;
    private List removeValues;
    private PlotValue plotValue;

    public void detach()
    {
        plotValues = null;
        removeValues = null;
        plotValue = null;

        super.detach();
    }

    /**
     *  Invokes {@link #getPlotValues()}, which ensures that (on the very first request cycle),
     *  the persistent values property is set <em>before</em> the page recorder is locked.
     * 
     **/

    public void beginResponse(IMarkupWriter writer, IRequestCycle cycle) throws RequestCycleException
    {
        getPlotValues();
    }

    public List getPlotValues()
    {
        if (plotValues == null)
        {
            setPlotValues(new ArrayList());

            plotValues.add(new PlotValue("Fred", 10));
            plotValues.add(new PlotValue("Barney", 15));
            plotValues.add(new PlotValue("Dino", 7));
        }

        return plotValues;
    }

    public void setPlotValues(List plotValues)
    {
        this.plotValues = plotValues;

        fireObservedChange("plotValues", plotValues);
    }

    public PlotValue getPlotValue()
    {
        return plotValue;
    }

    public void setPlotValue(PlotValue plotValue)
    {
        this.plotValue = plotValue;
    }

    /**
     *  Invoked during the render; always returns false.
     * 
     **/

    public boolean isMarkedForDeletion()
    {
        return false;
    }

    /**
     *  Invoked by the deleted checkbox (for each plotValue).  If true,
     *  the the current plotValue is added to the list of plotValues to
     *  remove (though the actual removing is done inside {@link #delete(IRequestCycle)},
     *  after the loop.
     *
     **/

    public void setMarkedForDeletion(boolean value)
    {
        if (value)
        {
            if (removeValues == null)
                removeValues = new ArrayList();

            removeValues.add(plotValue);

            // Deleting things screws up the validation delegate.
            // That's because the errors are associated with the form name
            // (not the component id), and deleting elements causes
            // all the names to shift.

            IValidationDelegate delegate = (IValidationDelegate) getBeans().getBean("delegate");

            delegate.clear();
        }
    }

    /**
     *  Form listener method; does nothing since we want to stay on this page.
     * 
     **/

    public void submit(IRequestCycle cycle)
    {
    }

    /**
     *  Listener method for the add button, adds an additional (blank) plot value.
     * 
     **/

    public void add(IRequestCycle cycle)
    {
        plotValues.add(new PlotValue());
    }

    /**
     *  Listener method for the remove button, removes any checked plot values.
     * 
     *  @see #setMarkedForDeletion(boolean)
     * 
     **/

    public void delete(IRequestCycle cycle)
    {
        if (removeValues != null)
            plotValues.removeAll(removeValues);
    }

    private IAsset chartImageAsset;

    public IAsset getChartImageAsset()
    {
        if (chartImageAsset == null)
            chartImageAsset = new ChartAsset(getRequestCycle(), this);

        return chartImageAsset;
    }

    /**
     *  This method is invoked by the service (in a seperate request cycle from all the form handling stuff).
     *  The {@link #getChartImageAsset()} method provides an {@link IAsset} that is handled by the 
     *  {@link ChartService}, and the asset encodes the identity of this page.
     * 
     **/

    public JFreeChart getChart()
    {
        DefaultPieDataset data = new DefaultPieDataset();

        int count = plotValues.size();

        for (int i = 0; i < count; i++)
        {
            PlotValue pv = (PlotValue) plotValues.get(i);

            String name = pv.getName();

            if (Tapestry.isNull(name))
                name = "<New>";

            data.setValue(name, new Integer(pv.getValue()));
        }

        JFreeChart result = ChartFactory.createPieChart("Pie Chart", data, false);

        result.setBackgroundPaint(Color.decode("#ffffcc"));

        return result;
    }

}