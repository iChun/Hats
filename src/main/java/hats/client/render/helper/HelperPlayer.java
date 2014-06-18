package hats.client.render.helper;

import hats.api.RenderOnEntityHelper;
import hats.client.gui.GuiHatSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
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
		return (ent == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F)) ? (ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 23F/16F : 21F/16F : 24.1F/16F) + 0.22F : (ent.isSneaking() ? ent == Minecraft.getMinecraft().thePlayer ? 23F/16F : 21F/16F : 24.1F/16F);
	}
	
	@Override
	public float getOffsetPointVert(EntityLivingBase ent)
	{
		return (ent == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F)) ? ent.isSneaking() ? 0.31F : 0.25F : 8F/16F;
	}

    @Override
    public float getOffsetPointHori(EntityLivingBase ent)
    {
        return (ent == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F)) ? -0.175F : 0.0F;
    }

    @Override
    public float getPrevRotationYaw(EntityLivingBase living)
    {
        return (living == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F)) ? living.prevRotationYaw : living.prevRotationYawHead;
    }

    @Override
    public float getRotationYaw(EntityLivingBase living)
    {
        return (living == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F)) ? living.rotationYaw : living.rotationYawHead;
    }

}
