package me.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import net.minecraft.util.math.MathHelper;

public class HelperBat extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityBat.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.config.hatBat == 1;
	}

    @Override
    public float getRotatePointHori(EntityLivingBase ent)
    {
        return 0F/16F;
    }

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return 0F/16F;
    }

    @Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 24F/16F + MathHelper.cos((ent.ticksExisted + renderTick) * 0.3F) * 0.275F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 3F/16F;
	}

    @Override
    public float getHatScale(EntityLivingBase ent)
    {
        if(((EntityBat)ent).getIsBatHanging())
        {
            return 0.0F;
        }
        return 0.8F;
    }
}
