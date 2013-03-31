package hats.client.render;

import java.awt.image.BufferedImage;

import hats.client.core.ClientProxy;
import hats.client.gui.GuiHatSelection;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.entity.EntityHat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;

import org.lwjgl.opengl.GL11;

public class RenderHat extends Render 
{

	public RenderHat()
	{
		shadowSize = 0.0F;
	}
	
    public void renderHat(EntityHat hat, double par2, double par4, double par6, float par8, float par9)
    {
    	if(!hat.hatName.equalsIgnoreCase("") && hat.player != null)
    	{
    		boolean firstPerson = (hat.player == Minecraft.getMinecraft().renderViewEntity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative || Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection) && RenderManager.instance.playerViewY == 180.0F));
    		
    		if((Hats.renderInFirstPerson == 1 && firstPerson || !firstPerson) && !hat.player.getHasActivePotion())
    		{
		    	ModelHat model = ClientProxy.models.get(hat.hatName);
		    	
	    		if(Minecraft.getMinecraft().thePlayer.username.equalsIgnoreCase("Notch") && !hat.player.username.equalsIgnoreCase("Notch"))
	    		{
	    		}
	    		

		    	if(model != null)
		    	{
			        GL11.glPushMatrix();
			        
		            GL11.glEnable(GL11.GL_BLEND);
		            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			        
			        GL11.glTranslatef((float)par2, (float)par4 + (Minecraft.getMinecraft().renderViewEntity != hat.player ? -0.06F : 0.0F ) + (hat.player != null && hat.player.isSneaking() ? Minecraft.getMinecraft().renderViewEntity != hat.player ? -0.17F : -0.05F : 0.0F), (float)par6);
			        GL11.glRotatef(180.0F - par8, 0.0F, 1.0F, 0.0F);
			        
			        GL11.glRotatef(hat.rotationPitch, -1.0F, 0.0F, 0.0F);
			        
			        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        
			        BufferedImage image = ClientProxy.bufferedImages.get(hat.hatName);
			        
			        if (image != null)
			        {
			            if (ClientProxy.bufferedImageID.get(image) == -1)
			            {
			            	ClientProxy.bufferedImageID.put(image, Minecraft.getMinecraft().renderEngine.allocateAndSetupTexture(image));
			            }
	
			            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ClientProxy.bufferedImageID.get(image));
			            Minecraft.getMinecraft().renderEngine.resetBoundTexture();
			        }
	
			        GL11.glScalef(-1.0F, -1.0F, 1.0F);
			        
			        model.render(hat, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			        
			        GL11.glDisable(GL11.GL_BLEND);
			        
			        GL11.glPopMatrix();
		    	}
		    	else
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

}
