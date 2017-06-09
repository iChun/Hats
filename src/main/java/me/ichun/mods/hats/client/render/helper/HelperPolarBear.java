package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPolarBear;

public class HelperPolarBear extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityPolarBear.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatPolarBear == 1;
	}

    @Override
    public float getRotatePointHori(EntityLivingBase ent)
    {
        return 16F/16F;
    }

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return -1F/16F;
    }

    @Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 12F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 5F/16F;
	}

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        return 1F;
    }
}
