package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class HelperPlayer extends RenderOnEntityHelper 
{
	@Override
	public Class helperForClass() 
	{
		return EntityPlayer.class;
	}

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 23F/16F : 21F/16F : 24.1F/16F ;
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 8F/16F;
	}
}
