package us.ichun.mods.hats.client.render.helper;

import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import us.ichun.mods.hats.common.Hats;

public class HelperCow extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityCow.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatCow == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 20.2F/16F;
	}

	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 8F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 3.5F/16F;
	}

	@Override
	public float getOffsetPointHori(EntityLivingBase ent)
	{
		return 2F/16F;
	}

}
