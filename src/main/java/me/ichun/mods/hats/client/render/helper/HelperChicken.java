package me.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;

public class HelperChicken extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityChicken.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatChicken == 1;
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 9F/16F;
	}

	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 4F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 6.15F/16F;
	}

	@Override
	public float getHatScale(EntityLivingBase ent)
	{
		return 0.5F;
	}


}
