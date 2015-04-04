package us.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;

public class HelperOcelot extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityOcelot.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatOcelot == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return ((EntityOcelot)ent).isSitting() ? 12.4F/16F : ent.isSneaking() ? 7F/16F : 9.1F/16F;
	}

	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return ((EntityOcelot)ent).isSitting() ? 8F/16F : 9F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 2F/16F;
	}

	@Override
	public float getHatScale(EntityLivingBase ent)
	{
		return 6F/8F;
	}

}
