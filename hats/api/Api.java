package hats.api;

import morph.common.Morph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

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
