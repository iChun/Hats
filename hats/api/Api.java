package hats.api;

import hats.client.core.HatInfoClient;


public final class Api 
{
	/**
	 * Registers this RenderOnEntityHelper class with the Hats mod.
	 * @param helper to be registered.
	 */
	public static void registerHelper(RenderOnEntityHelper helper)
	{
		try {
			Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("registerHelper", RenderOnEntityHelper.class).invoke(null, helper);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Creates an object storing the relative hat info, used for when rendering hats.
	 * @param Hat Name
	 * @param Red value, 0 - 255
	 * @param Green value, 0 - 255
	 * @param Blue value, 0 - 255
	 * @return Hat info object
	 */
	public static Object createHatInfo(String hatName, int r, int g, int b)
	{
		try {
			return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("createHatInfo", String.class, int.class, int.class, int.class).invoke(null, hatName, r, g, b);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Renders the Hat using the provided hat info.
	 * You have to pre-translate to the hat position first before translation.
	 * @param Hat Info (created using createHatInfo)
	 * @param Alpha levels (1.0F for opaque)
	 * @param Hat Scale
	 * @param Scale - X axis
	 * @param Scale - Y axis
	 * @param Scale - Z axis
	 * @param Render Yaw Offset
	 * @param Rotation Yaw Offset
	 * @param Rotation Pitch Offset
	 * @param Rotation Roll Offset (Stuff like the wolf's interest when you hold bones)
	 * @param Rotation Point Vertical Offset
	 * @param Rotation Point Horizontal Offset
	 * @param Rotation Point Sideways Offset
	 * @param Render Point Vertical Offset
	 * @param Render Point Horizontal Offset
	 * @param Render Point Sideways Offset
	 * @param Force Render
	 * @param Bind Hat Texture when rendering
	 * @param Render Tick
	 */
	public static void renderHat(Object info, float alpha, float hatScale, float mobRenderScaleX, float mobRenderScaleY, float mobRenderScaleZ, float renderYawOffset, float rotationYaw, float rotationPitch, float rotationRoll, float rotatePointVert, float rotatePointHori, float rotatePointSide, float offsetVert, float offsetHori, float offsetSide, boolean forceRender, boolean bindTexture, float renderTick)
	{
		try {
			Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("renderHat", Object.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, boolean.class, boolean.class, float.class).invoke(null, info, alpha, hatScale, mobRenderScaleX, mobRenderScaleY, mobRenderScaleZ, renderYawOffset, rotationYaw, rotationPitch, rotationRoll, rotatePointVert, rotatePointHori, rotatePointSide, offsetVert, offsetHori, offsetSide, forceRender, bindTexture, renderTick);
		} catch (Exception e) {
		}
	}
}
