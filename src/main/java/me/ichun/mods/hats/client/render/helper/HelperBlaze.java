package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import me.ichun.mods.hats.common.Hats;

public class HelperBlaze extends RenderOnEntityHelper
{

	@Override
	public Class helperForClass() 
	{
		return EntityBlaze.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatBlaze == 1;
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 24F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 4F/16F;
	}
}
