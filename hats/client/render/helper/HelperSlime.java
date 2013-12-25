package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;

public class HelperSlime extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntitySlime.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.hatSlime == 1;
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
	
	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 8F/16F;
	}	
}
