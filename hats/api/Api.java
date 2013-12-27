package hats.api;


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
}
