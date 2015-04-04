package us.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.Hats;

public class HelperPig extends RenderOnEntityHelper 
{

	@Override
	public Class helperForClass() 
	{
		return EntityPig.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatPig == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 12.35F/16F;
	}

	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 6F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 3.7F/16F;
	}

	@Override
	public float getOffsetPointHori(EntityLivingBase ent)
	{
		return 4F/16F;
	}

}
