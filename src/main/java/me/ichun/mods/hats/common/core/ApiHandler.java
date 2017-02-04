package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.client.core.HatInfoClient;
import me.ichun.mods.hats.client.render.HatRendererHelper;
import me.ichun.mods.hats.common.Hats;


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
			boolean exists = CommomProxy.renderHelpers.get(helper.helperForClass()) != null;
			CommomProxy.renderHelpers.put(helper.helperForClass(), helper);
			Hats.console((exists ? "Overriding" : "Registering") + " hat render helper for " + helper.helperForClass().getName());
		}
	}

    public static Object createHatInfo(String hatName, int r, int g, int b, int alpha)
    {
        return new HatInfoClient(hatName, r, g, b, alpha);
    }

    public static Object createHatInfo(String hatName, int r, int g, int b)
	{
		return new HatInfoClient(hatName, r, g, b, 255);
	}

    public static Object getRandomHatInfoWithServerWeightage(int r, int g, int b, int alpha)
    {
        return new HatInfoClient(HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), true).hatName, r, g, b, alpha);
    }

    public static Object getRandomHatInfoWithServerWeightage(int r, int g, int b)
    {
        return new HatInfoClient(HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), true).hatName, r, g, b, 255);
    }

    public static Object getRandomHatInfo(int r, int g, int b, int alpha)
    {
        return new HatInfoClient(HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), false).hatName, r, g, b, alpha);
    }

    public static Object getRandomHatInfo(int r, int g, int b)
	{
        return new HatInfoClient(HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), false).hatName, r, g, b, 255);
	}
	
	public static void renderHat(Object info, float alpha, float hatScale, float mobRenderScaleX, float mobRenderScaleY, float mobRenderScaleZ, float renderYawOffset, float rotationYaw, float rotationPitch, float rotationRoll, float rotatePointVert, float rotatePointHori, float rotatePointSide, float offsetVert, float offsetHori, float offsetSide, boolean forceRender, boolean bindTexture, float renderTick)
	{
		if(!(info instanceof HatInfoClient))
		{
			Hats.console("Received render command with non-hat info object!", true);
		}
		else
		{
			HatRendererHelper.renderHat((HatInfoClient)info, alpha, hatScale, mobRenderScaleX, mobRenderScaleY, mobRenderScaleZ, renderYawOffset, rotationYaw, rotationPitch, rotationRoll, rotatePointVert, rotatePointHori, rotatePointSide, offsetVert, offsetHori, offsetSide, forceRender, bindTexture, renderTick);
		}
	}

}
