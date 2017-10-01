package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractIllager;

public class HelperIllager extends RenderOnEntityHelper {

	@Override
	public Class helperForClass()
	{
		return AbstractIllager.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatVillager == 1;
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 24F/16F ;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 10F/16F;
	}

}
