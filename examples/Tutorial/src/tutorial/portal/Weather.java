package tutorial.portal;

import com.primix.tapestry.*;

/**
 *  This is much simpler than the others, and mostly is here to demonstrate
 *  how to include {@link com.primix.tapestry.link.Direct} links in a Portlet
 *  content.
 *
 *  @author Howard Ship
 *  @version $Id$
 *
 */

public class Weather 
	extends BasePage
{
	private boolean weekend = false;
	
	public void detach()
	{
		weekend = false;
		
		super.detach();
	}
	
	public void setWeekend(boolean value)
	{
		weekend = value;
		fireObservedChange("weekend", value);
	}
	
	public boolean isWeekend()
	{
		return weekend;
	}
	
	public String getForecast()
	{
		if (weekend)
			return "Dismal, pelting rain, 47 - 52.  Enjoy your time off.";
		
		return "Sunny, bright, 76 - 82.  Now, get back to your cube.";
	}

	public void selectWeekend(IRequestCycle cycle)
	{
		setWeekend(true);
	}
	
	public void selectWeekday(IRequestCycle cycle)
	{
		setWeekend(false);
	}
	
	public String getWeekdayClass()
	{
		return weekend ? null : "selected";
	}
	
	public String getWeekendClass()
	{
		return weekend ? "selected" : null;
	}
}
