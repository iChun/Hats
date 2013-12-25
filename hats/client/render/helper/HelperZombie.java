package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.common.Hats;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;

public class HelperZombie extends RenderOnEntityHelper {

	@Override
	public Class helperForClass() 
	{
		return EntityZombie.class;
	}

	@Override
	public boolean canWearHat(EntityLivingBase living)
	{
		return Hats.hatZombie == 1;
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 23F/16F : 21F/16F : 24F/16F ;
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return ((EntityZombie)ent).isVillager() ? 10F/16F : 8F/16F;
	}
}
