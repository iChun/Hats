package hats.common.core;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;


public class ApiHandler 
{
	public static void registerHelper(RenderOnEntityHelper helper)
	{
		if(helper.helperForClass() == null)
		{
			Hats.console("Received hat render helper with null class!", true);
		}
		else
		{
			boolean exists = CommonProxy.renderHelpers.get(helper.helperForClass()) != null;
			CommonProxy.renderHelpers.put(helper.helperForClass(), helper);
			Hats.console((exists ? "Overriding" : "Registering") + " hat render helper for " + helper.helperForClass().getName());
		}
	}
}
