package hats.client.render;

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
    	ModelHat model = ClientProxy.models.get(hat.hatName);
    	if(model != null)
    	{
	        GL11.glPushMatrix();
	        GL11.glTranslatef((float)par2, (float)par4 + (Minecraft.getMinecraft().renderViewEntity == null ? 0.0F : Minecraft.getMinecraft().renderViewEntity.yOffset - Minecraft.getMinecraft().renderViewEntity.getEyeHeight()), (float)par6);
	        GL11.glRotatef(180.0F - par8, 0.0F, 1.0F, 0.0F);
	        this.loadTexture("/mods/portalgun/textures/model/skin_radio.png");
	        GL11.glScalef(-1.0F, -1.0F, 1.0F);
	        model.render(hat, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
	        GL11.glPopMatrix();
    	}
    }
	
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderHat((EntityHat)par1Entity, par2, par4, par6, par8, par9);
    }

}
