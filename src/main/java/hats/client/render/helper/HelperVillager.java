package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;

public class HelperVillager extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityVillager.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.getInt("hatVillager") == 1;
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
