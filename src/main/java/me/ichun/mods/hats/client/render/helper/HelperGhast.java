package me.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;

public class HelperGhast extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityGhast.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatGhast == 1;
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
		return 14.5F/16F;
	}
	
	@Override
	public float getHatScale(EntityLivingBase ent)
	{
		return 2.0F;
	}
}
