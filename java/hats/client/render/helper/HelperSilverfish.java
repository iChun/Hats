package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySilverfish;

public class HelperSilverfish extends RenderOnEntityHelper 
{

	/**
	 * Warning! This class is not finished!
	 */
	
	@Override
	public Class helperForClass() 
	{
		return EntitySilverfish.class;
	}

	@Override
	public float getPrevRotationYaw(EntityLivingBase living)
	{
		return living.prevRenderYawOffset;
	}

	@Override
	public float getRotationYaw(EntityLivingBase living)
	{
		return living.renderYawOffset;
	}

	@Override
	public float getPrevRotationPitch(EntityLivingBase living)
	{
		return 0.0F;
	}

	@Override
	public float getRotationPitch(EntityLivingBase living)
	{
		return 0.0F;
	}
}
