package us.ichun.mods.hats.client.render.helper;

import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;

public class HelperSquid extends RenderOnEntityHelper
{

	@Override
	public Class helperForClass() 
	{
		return EntitySquid.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatSquid == 1;
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
		return -((EntitySquid)living).prevSquidPitch;
	}
	
	@Override
	public float getRotationPitch(EntityLivingBase living)
	{
		return -((EntitySquid)living).squidPitch;
	}
	
	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 8F/16F;
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 5F/16F;
	}
	
	@Override
	public float getHatScale(EntityLivingBase ent)
	{
		return 1.5F;
	}

	
}
