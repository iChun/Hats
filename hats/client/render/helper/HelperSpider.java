package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;

public class HelperSpider extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntitySpider.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.hatSpider == 1;
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return 9.2F/16F;
	}

	@Override
	public float getRotatePointHori(EntityLivingBase ent)
	{
		return 3F/16F;
	}

	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 3.8F/16F;
	}

	@Override
	public float getOffsetPointHori(EntityLivingBase ent)
	{
		return 4F/16F;
	}

}
