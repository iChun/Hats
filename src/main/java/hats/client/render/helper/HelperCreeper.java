package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;

public class HelperCreeper extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityCreeper.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.getInt("hatCreeper") == 1;
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
