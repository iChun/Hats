package hats.client.render;

import java.awt.image.BufferedImage;

import hats.client.core.ClientProxy;
import hats.client.model.ModelHat;
import hats.common.entity.EntityHat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

public class RenderHat extends Render 
{

	public RenderHat()
	{
		shadowSize = 0.0F;
	}
	
    public void renderHat(EntityHat hat, double par2, double par4, double par6, float par8, float par9)
    {
    	if(!hat.hatName.equalsIgnoreCase(""))
    	{
	    	ModelHat model = ClientProxy.models.get(hat.hatName);
	    	if(model != null)
	    	{
		        GL11.glPushMatrix();
		        GL11.glTranslatef((float)par2, (float)par4 + (Minecraft.getMinecraft().renderViewEntity == null ? 0.0F : Minecraft.getMinecraft().renderViewEntity.yOffset - Minecraft.getMinecraft().renderViewEntity.getEyeHeight()), (float)par6);
		        GL11.glRotatef(180.0F - par8, 0.0F, 1.0F, 0.0F);
		        
		        GL11.glRotatef(hat.rotationPitch, -1.0F, 0.0F, 0.0F);
		        
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
		        GL11.glPopMatrix();
	    	}
    	}
    }
	
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderHat((EntityHat)par1Entity, par2, par4, par6, par8, par9);
    }

}
