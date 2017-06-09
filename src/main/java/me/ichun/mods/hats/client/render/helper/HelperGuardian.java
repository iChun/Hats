package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;

public class HelperGuardian extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityGuardian.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatGuardian == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return -9F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 25F/16F;
	}
}
