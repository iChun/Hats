package hats.client.render;

import java.awt.image.BufferedImage;

import hats.client.core.ClientProxy;
import hats.client.gui.GuiHatSelection;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.entity.EntityHat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
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
    	if(!hat.hatName.equalsIgnoreCase("") && hat.parent != null && !hat.parent.isPlayerSleeping() && hat.parent.isEntityAlive() && Hats.renderHats == 1 && hat.render && (hat.parent instanceof EntityPlayer && (hat.renderingParent instanceof EntityPlayer || HatHandler.canMobHat(hat.renderingParent)) || !(hat.parent instanceof EntityPlayer)))
    	{
    		boolean firstPerson = (hat.parent == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F));

    		if((Hats.renderInFirstPerson == 1 && firstPerson || !firstPerson) && !hat.renderingParent.isInvisible() )
    		{
		    	ModelHat model = ClientProxy.models.get(hat.hatName);
		    	
		    	if(model != null)
		    	{
		    		if(!(hat.parent instanceof EntityPlayer) &&  Hats.proxy.tickHandlerClient.currentHatRenders >= Hats.maxHatRenders && !firstPerson)
		    		{
		    			return;
		    		}
		    		
			        GL11.glPushMatrix();
			        
		            GL11.glEnable(GL11.GL_BLEND);
		            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			        
		            float rotYawHead = interpolateRotation(hat.getPrevRotationYaw(), hat.getRotationYaw(), par9);
		            
			        GL11.glTranslatef((float)par2, (float)par4 + HatHandler.getVerticalRenderOffset(hat.renderingParent), (float)par6);
			        if(hat.parent == Minecraft.getMinecraft().renderViewEntity && hat.renderingParent != hat.parent)
			        {
			        	GL11.glTranslatef(0.0F, -hat.parent.yOffset, 0.0F);
			        }
			        GL11.glRotatef(180.0F - par8, 0.0F, 1.0F, 0.0F);
			        
			        GL11.glRotatef(interpolateRotation(hat.getPrevRotationPitch(), hat.getRotationPitch(), par9), -1.0F, 0.0F, 0.0F);
			        
			        GL11.glRotatef(180.0F - par8, 0.0F, -1.0F, 0.0F);
			        GL11.glTranslatef((HatHandler.getHorizontalPostRotateOffset(hat.renderingParent) * (float)Math.sin(Math.toRadians(rotYawHead))), HatHandler.getVerticalPostRotateOffset(hat.renderingParent), -(HatHandler.getHorizontalPostRotateOffset(hat.renderingParent) * (float)Math.cos(Math.toRadians(rotYawHead))));
			        GL11.glRotatef(180.0F - par8, 0.0F, 1.0F, 0.0F);
			        
			        float scale = HatHandler.getRenderScale(hat.renderingParent);
			        GL11.glScalef(scale, scale, scale);
			        
			        if(hat.reColour > 0)
			        {
			        	float diffR = hat.getR() - hat.prevR;
			        	float diffG = hat.getG() - hat.prevG;
			        	float diffB = hat.getB() - hat.prevB;
			        	
			        	float rendTick = par9;
			        	if(rendTick > 1.0F)
			        	{
			        		rendTick = 1.0F;
			        	}
			        	
			        	diffR *= (float)(hat.reColour - rendTick) / 20F;
			        	diffG *= (float)(hat.reColour - rendTick) / 20F;
			        	diffB *= (float)(hat.reColour - rendTick) / 20F;
			        	
			        	GL11.glColor4f((float)(hat.getR() - diffR) / 255.0F, (float)(hat.getG() - diffG) / 255.0F, (float)(hat.getB() - diffB) / 255.0F, 1.0F);
			        }
			        else
			        {
			        	GL11.glColor4f((float)hat.getR() / 255.0F, (float)hat.getG() / 255.0F, (float)hat.getB() / 255.0F, 1.0F);
			        }
			        
			        BufferedImage image = ClientProxy.bufferedImages.get(hat.hatName);
			        
//			    	if(Minecraft.getMinecraft().thePlayer.username.equalsIgnoreCase("Notch") && hat.parent instanceof EntityPlayer && ((EntityPlayer)hat.parent).username.equalsIgnoreCase("Notch"))
//			    	{
////			    		System.out.println(par8);
////			    		System.out.println((HatHandler.getHorizontalPostRotateOffset(hat.parent) * (float)Math.sin(Math.toRadians(rotYawHead))));
////			    		System.out.println(rotYawHead);
//			    	}
			        
			        if (image != null)
			        {
			            if (ClientProxy.bufferedImageID.get(image) == -1)
			            {
			            	ClientProxy.bufferedImageID.put(image, TextureUtil.func_110987_a(TextureUtil.func_110996_a(), image));
			            }
	
			            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ClientProxy.bufferedImageID.get(image));
			        }
	
			        GL11.glScalef(-1.0F, -1.0F, 1.0F);
			        
			        model.render(hat, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			        
			        GL11.glDisable(GL11.GL_BLEND);
			        
			        GL11.glPopMatrix();
			        
			        Hats.proxy.tickHandlerClient.currentHatRenders++;
		    	}
		    	else if(!HatHandler.reloadingHats)
		    	{
		    		if(!Hats.proxy.tickHandlerClient.requestedHats.contains(hat.hatName))
		    		{
		    			HatHandler.requestHat(hat.hatName, null);
		    			Hats.proxy.tickHandlerClient.requestedHats.add(hat.hatName);
		    		}
		    	}
    		}
    	}
    }
	
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderHat((EntityHat)par1Entity, par2, par4, par6, par8, par9);
    }
    
    public float interpolateRotation(float par1, float par2, float par3)
    {
        float f3;

        for (f3 = par2 - par1; f3 < -180.0F; f3 += 360.0F)
        {
            ;
        }

        while (f3 >= 180.0F)
        {
            f3 -= 360.0F;
        }

        return par1 + par3 * f3;
    }

	@Override
	protected ResourceLocation func_110775_a(Entity entity) 
	{
		return AbstractClientPlayer.field_110314_b;
	}

}
