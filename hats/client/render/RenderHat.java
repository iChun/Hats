package hats.client.render;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;

import hats.client.core.ClientProxy;
import hats.client.core.HatInfoClient;
import hats.client.gui.GuiHatSelection;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.entity.EntityHat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderHat extends Render 
{

	public RenderHat()
	{
		shadowSize = 0.0F;
	}
	
    public void renderHat(EntityHat hat, double par2, double par4, double par6, float par8, float par9)
    {
    	if(hat.info != null && !hat.info.hatName.equalsIgnoreCase("") && !hat.renderingParent.isPlayerSleeping() && hat.renderingParent.isEntityAlive() && Hats.renderHats == 1 && hat.render && (hat.parent instanceof EntityPlayer && (hat.renderingParent instanceof EntityPlayer || HatHandler.canMobHat(hat.renderingParent)) || !(hat.parent instanceof EntityPlayer)))
    	{
    		boolean firstPerson = (hat.parent == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F));

    		if((Hats.renderInFirstPerson == 1 && firstPerson || !firstPerson) && !hat.renderingParent.isInvisible())
    		{
	    		GL11.glPushMatrix();

    			boolean isPlayer = hat.parent instanceof EntityPlayer;
    			if(isPlayer && hat.parent == Minecraft.getMinecraft().renderViewEntity && hat.parent.isSneaking())
    			{
    				GL11.glTranslatef(0.0F, -0.075F, 0.0F);
    			}
    			
				float renderTick = par9;
				
		        GL11.glTranslated(par2, par4, par6);
	            GL11.glTranslatef(0.0F, -hat.parent.yOffset, 0.0F);
	            GL11.glTranslatef(0.0F, (float)-(hat.lastTickPosY - hat.parent.lastTickPosY) + (float)((hat.parent.boundingBox.minY + hat.parent.yOffset) - (hat.parent.posY)), 0.0F);
	            

	            int i = hat.renderingParent.getBrightnessForRender(renderTick);
	            int j = i % 65536;
	            int k = i / 65536;
	            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            
	    		FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
	    		FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);

	    		GL11.glPushMatrix();
	    		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
	    		Render rend = RenderManager.instance.getEntityRenderObject(hat.renderingParent);
	    		HatRendererHelper.invokePreRenderCallback(rend, rend.getClass(), hat.renderingParent, renderTick);
	    		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
	    		GL11.glPopMatrix();

	    		float prevScaleX = buffer1.get(0) / buffer.get(0);
	    		float prevScaleY = buffer1.get(5) / buffer.get(5);
	    		float prevScaleZ = buffer1.get(8) / buffer.get(8);
	            
				for(int ii = 0 ; ii < 1; ii ++)
				{
					HatRendererHelper.renderHat(hat.info, HatRendererHelper.getHatScale(hat.renderingParent), prevScaleX, prevScaleY, prevScaleZ, HatRendererHelper.interpolateRotation(HatRendererHelper.getPrevRenderYaw(hat.renderingParent), HatRendererHelper.getRenderYaw(hat.renderingParent), renderTick), HatRendererHelper.interpolateRotation(HatRendererHelper.getPrevRotationYaw(hat.renderingParent), HatRendererHelper.getRotationYaw(hat.renderingParent), renderTick), HatRendererHelper.interpolateRotation(HatRendererHelper.getPrevRotationPitch(hat.renderingParent), HatRendererHelper.getRotationPitch(hat.renderingParent), renderTick) + ii * 90F, HatRendererHelper.getRotatePointVert(hat.renderingParent), HatRendererHelper.getRotatePointHori(hat.renderingParent), HatRendererHelper.getOffsetPointVert(hat.renderingParent), HatRendererHelper.getOffsetPointHori(hat.renderingParent), isPlayer, renderTick);
				}
				
				GL11.glPopMatrix();
    		}
    	}
    }
	
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderHat((EntityHat)par1Entity, par2, par4, par6, par8, par9);
    }
    
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) 
	{
		return AbstractClientPlayer.locationStevePng;
	}

}
