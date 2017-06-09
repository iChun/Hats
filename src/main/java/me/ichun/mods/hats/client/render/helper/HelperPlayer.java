package me.ichun.mods.hats.client.render.helper;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.client.gui.GuiHatSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
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
	    float point = (ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 21F/16F : 17.5F/16F : 24.1F/16F);
		return isFirstPerson(ent) ? point + 0.22F : point;
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return isFirstPerson(ent) ? ent.isSneaking() ? 0.31F : 0.25F : 8F/16F;
	}

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return isFirstPerson(ent) ? -0.175F : 0.0F;
    }

    @Override
    public float getPrevRotationYaw(EntityLivingBase ent)
    {
        return isFirstPerson(ent) ? ent.prevRotationYaw : ent.prevRotationYawHead;
    }

    @Override
    public float getRotationYaw(EntityLivingBase ent)
    {
        return isFirstPerson(ent) ? ent.rotationYaw : ent.rotationYawHead;
    }
    
    public boolean isFirstPerson(EntityLivingBase ent)
    {
        return (ent == Minecraft.getMinecraft().getRenderViewEntity() &&
                Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 &&
                !(
                        (Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) &&
                                Minecraft.getMinecraft().getRenderManager().playerViewY == 180.0F
                )
        );
    }
}
