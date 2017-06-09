package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;

public class HelperRabbit extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityRabbit.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatRabbit == 1;
	}

    @Override
    public float getRotatePointHori(EntityLivingBase ent)
    {
        return 0.4F/16F;
    }

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return 1.7F/16F;
    }

    @Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 4F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 3.2F/16F;
	}

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        return 0.375F;
    }
}
