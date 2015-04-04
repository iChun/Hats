package us.ichun.mods.hats.client.render.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;

public class HelperVillager extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityVillager.class;
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
