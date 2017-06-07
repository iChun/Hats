package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import me.ichun.mods.hats.common.Hats;

public class HelperCreeper extends RenderOnEntityHelper
{

	@Override
	public Class helperForClass() 
	{
		return EntityCreeper.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatCreeper == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 20.15F/16F;
	}


	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 8F/16F;
	}
}
