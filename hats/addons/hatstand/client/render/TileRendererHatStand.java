package hats.addons.hatstand.client.render;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import hats.client.core.ClientProxy;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.HatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StringUtils;

public class TileRendererHatStand extends TileEntitySpecialRenderer 
{
	public static TileRendererHatStand renderer;
	public static RenderBlocks renderBlocks = new RenderBlocks();
	
	public ModelSkeletonHead head32 = new ModelSkeletonHead(0, 0, 64, 32);
	public ModelSkeletonHead head64 = new ModelSkeletonHead(0, 0, 64, 64);
	
	public TileRendererHatStand()
	{
	}
	
    public void setTileEntityRenderer(TileEntityRenderer par1TileEntityRenderer)
    {
        super.setTileEntityRenderer(par1TileEntityRenderer);
        renderer = this;
    }
	
	public void renderHatStand(TileEntityHatStand stand, double d, double d1, double d2, float f)
	{
		GL11.glPushMatrix();
		
		GL11.glTranslated(d + 0.5D, d1 + 0.4D, d2 + 0.5D);
        GL11.glScalef(-1.0F, -1.0F, 1.0F);

		GL11.glRotatef((stand.orientation - 1) * 90F, 0.0F, 1.0F, 0.0F);
		
        if(!stand.isOnFloor)
        {
        	if(stand.hasBase)
        	{
        		GL11.glTranslated(0.0D, 0.2D, 0.1D);
        	}
        	else
        	{
        		GL11.glTranslated(0.0D, 0.2D, 0.25D);
        	}
        }
        else if(!stand.hasStand)
        {
        	if(!stand.hasBase)
        	{
        		GL11.glTranslated(0.0D, 0.45D, 0.0D);
        	}
        	else
        	{
        		GL11.glTranslated(0.0D, 0.3D, 0.0D);
        	}
        }

		if(stand.head > 0)
		{
			if(!stand.hasStand)
			{
				GL11.glTranslated(0.0D, -0.05D, 0.0D);
			}
			GL11.glDisable(GL11.GL_CULL_FACE);
			
			ModelSkeletonHead head = head32;

			switch(stand.head)
			{
            case 1:
            default:
                this.bindTextureByName("/mob/skeleton.png");
                break;
            case 2:
                this.bindTextureByName("/mob/skeleton_wither.png");
                break;
            case 3:
                this.bindTextureByName("/mob/zombie.png");
                head = head64;
                break;
            case 4:
                if (stand.headName != null && stand.headName.length() > 0)
                {
                    String s1 = "http://skins.minecraft.net/MinecraftSkins/" + StringUtils.stripControlCodes(stand.headName) + ".png";

                    if (!this.tileEntityRenderer.renderEngine.hasImageData(s1))
                    {
                        this.tileEntityRenderer.renderEngine.obtainImageData(s1, new ImageBufferDownload());
                    }

                    this.bindTextureByURL(s1, "/mob/char.png");
                }
                else
                {
                    this.bindTextureByName("/mob/char.png");
                }

                break;
            case 5:
            	this.bindTextureByName("/mob/char.png");
            	break;
            case 6:
                this.bindTextureByName("/mob/creeper.png");
			}
			
	        float f4 = 0.0625F;
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        head.render((Entity)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f4);
		}
		else
		{
			GL11.glTranslated(0.0D, 0.45D, 0.0D);
		}
		
		ModelHat model = ClientProxy.models.get(stand.hatName);
		if(model != null)
		{
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            GL11.glColor4f((float)stand.colourR / 255.0F, (float)stand.colourG / 255.0F, (float)stand.colourB / 255.0F, 1.0F);
            
	        BufferedImage image = ClientProxy.bufferedImages.get(stand.hatName);

	        if (image != null)
	        {
	            if (ClientProxy.bufferedImageID.get(image) == -1)
	            {
	            	ClientProxy.bufferedImageID.put(image, Minecraft.getMinecraft().renderEngine.allocateAndSetupTexture(image));
	            }

	            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ClientProxy.bufferedImageID.get(image));
	            Minecraft.getMinecraft().renderEngine.resetBoundTexture();
	        }
	        
	        GL11.glTranslated(0.0D, -0.038D, 0.0D);
	        
	        model.render((Entity)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
	        
	        GL11.glDisable(GL11.GL_BLEND);

		}
    	else if(!HatHandler.reloadingHats)
    	{
    		if(!Hats.proxy.tickHandlerClient.requestedHats.contains(stand.hatName))
    		{
    			HatHandler.requestHat(stand.hatName, null);
    			Hats.proxy.tickHandlerClient.requestedHats.add(stand.hatName);
    		}
    	}
		
        GL11.glPopMatrix();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d0, double d1,
			double d2, float f) {
		this.renderHatStand((TileEntityHatStand)tileentity, d0, d1, d2, f);

	}

}
