package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;

public class HelperSheep extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntitySheep.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.getInt("hatSheep") == 1;
    }
	
	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 18.2F/16F;
	}
	
	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 8F/16F;
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 4F/16F;
	}
	
	@Override
	public float getOffsetPointHori(EntityLivingBase ent)
	{
		return 3F/16F;
	}
	
	@Override
	public float getHatScale(EntityLivingBase ent)
	{
		if(((EntitySheep) ent).func_70894_j(1.0F) != 0.0F)
		{
			return 0.0F;
		}
		return 0.75F;
	}
}
