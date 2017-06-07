package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;

public class HelperEnderman extends RenderOnEntityHelper
{

	@Override
	public Class helperForClass() 
	{
		return EntityEnderman.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatEnderman == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 37.1F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return ((EntityEnderman)ent).isScreaming() ? 13F/16F : 8F/16F;
	}
}
