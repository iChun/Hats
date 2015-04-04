package us.ichun.mods.hats.api;

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
	 * @param hatName - Hat Name
	 * @param r - Red value, 0 - 255
	 * @param g - Green value, 0 - 255
	 * @param b - Blue value, 0 - 255
	 * @return Hat info object
	 */
    public static Object createHatInfo(String hatName, int r, int g, int b, int alpha)
    {
        try {
            return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("createHatInfo", String.class, int.class, int.class, int.class, int.class).invoke(null, hatName, r, g, b, alpha);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object createHatInfo(String hatName, int r, int g, int b)
	{
		try {
			return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("createHatInfo", String.class, int.class, int.class, int.class).invoke(null, hatName, r, g, b);
		} catch (Exception e) {
			return null;
		}
	}

    public static Object getRandomHatInfoWithServerWeightage(int r, int g, int b, int alpha)
    {
        try {
            return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("getRandomHatInfoWithServerWeightage", int.class, int.class, int.class, int.class).invoke(null, r, g, b, alpha);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getRandomHatInfoWithServerWeightage(int r, int g, int b)
    {
        try {
            return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("getRandomHatInfoWithServerWeightage", int.class, int.class, int.class).invoke(null, r, g, b);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getRandomHatInfo(int r, int g, int b, int alpha)
    {
        try {
            return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("getRandomHatInfo", int.class, int.class, int.class, int.class).invoke(null, r, g, b, alpha);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getRandomHatInfo(int r, int g, int b)
	{
		try {
			return (Object)Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("getRandomHatInfo", int.class, int.class, int.class).invoke(null, r, g, b);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Renders the Hat using the provided hat info.
	 * You have to pre-translate to the hat position first before translation.
	 * @param info - Hat Info (created using createHatInfo)
	 * @param alpha - Alpha levels (1.0F for opaque)
	 * @param hatScale - Hat Scale
	 * @param mobRenderScaleX - Scale - X axis
	 * @param mobRenderScaleY - Scale - Y axis
	 * @param mobRenderScaleZ - Scale - Z axis
	 * @param renderYawOffset - Render Yaw Offset
	 * @param rotationYaw - Rotation Yaw Offset
	 * @param rotationPitch - Rotation Pitch Offset
	 * @param rotationRoll - Rotation Roll Offset (Stuff like the wolf's interest when you hold bones)
	 * @param rotatePointVert - Rotation Point Vertical Offset
	 * @param rotatePointHori - Rotation Point Horizontal Offset
	 * @param rotatePointSide - Rotation Point Sideways Offset
	 * @param offsetVert - Render Point Vertical Offset
	 * @param offsetHori - Render Point Horizontal Offset
	 * @param offsetSide - Render Point Sideways Offset
	 * @param forceRender - Force Render
	 * @param bindTexture - Bind Hat Texture when rendering
	 * @param renderTick - Render Tick
	 */
	public static void renderHat(Object info, float alpha, float hatScale, float mobRenderScaleX, float mobRenderScaleY, float mobRenderScaleZ, float renderYawOffset, float rotationYaw, float rotationPitch, float rotationRoll, float rotatePointVert, float rotatePointHori, float rotatePointSide, float offsetVert, float offsetHori, float offsetSide, boolean forceRender, boolean bindTexture, float renderTick)
	{
		try {
			Class.forName("hats.common.core.ApiHandler").getDeclaredMethod("renderHat", Object.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, boolean.class, boolean.class, float.class).invoke(null, info, alpha, hatScale, mobRenderScaleX, mobRenderScaleY, mobRenderScaleZ, renderYawOffset, rotationYaw, rotationPitch, rotationRoll, rotatePointVert, rotatePointHori, rotatePointSide, offsetVert, offsetHori, offsetSide, forceRender, bindTexture, renderTick);
		} catch (Exception e) {
		}
	}
}
