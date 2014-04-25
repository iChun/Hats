package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;

public class HelperBlaze extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityBlaze.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.getInt("hatBlaze") == 1;
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
