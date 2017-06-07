package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGiantZombie;
import me.ichun.mods.hats.common.Hats;

public class HelperGiantZombie extends RenderOnEntityHelper
{

	@Override
	public Class helperForClass() 
	{
		return EntityGiantZombie.class;
	}

    @Override
    public boolean canWearHat(EntityLivingBase living)
    {
        return Hats.config.hatZombie == 1;
    }

	@Override
	public float getRotatePointVert(EntityLivingBase ent)
	{
		return ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 23F/16F : 21F/16F : 24F/16F ;
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return 8F/16F;
	}
}
