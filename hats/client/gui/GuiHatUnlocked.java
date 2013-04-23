package hats.client.gui;

import hats.client.core.ClientProxy;
import hats.client.model.ModelHat;
import hats.common.Hats;
import hats.common.core.HatHandler;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiHatUnlocked extends Gui
{
    /** Holds the instance of the game (Minecraft) */
    private Minecraft theGame;

    /** Holds the latest width scaled to fit the game window. */
    private int width;

    /** Holds the latest height scaled to fit the game window. */
    private int height;
    private String headerText;
    private String hatNameText;

    private long unlockedTime;
    
    private ArrayList<String> hatList = new ArrayList<String>(); 

    public GuiHatUnlocked(Minecraft par1Minecraft)
    {
        this.theGame = par1Minecraft;
        this.headerText = "\u00A7e" + "Hat Unlocked!";
        hatNameText = "";
    }

    public void queueHatUnlocked(String hat)
    {
    	if(!hatNameText.equalsIgnoreCase(hat))
    	{
	    	if(!hatList.contains(hat))
	    	{
	    		hatList.add(hat);
	    	}
	    	showNextHatUnlocked();
    	}
    }
    
    public void showNextHatUnlocked()
    {
    	if(hatList.size() > 0 && unlockedTime == 0L)
    	{
	        hatNameText = hatList.get(0);
	        unlockedTime = Minecraft.getSystemTime();
	        hatList.remove(0);
    	}
    }

    /**
     * Update the display of the achievement window to match the game window.
     */
    private void updateWindowScale()
    {
        GL11.glViewport(0, 0, this.theGame.displayWidth, this.theGame.displayHeight);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        this.width = this.theGame.displayWidth;
        this.height = this.theGame.displayHeight;
        ScaledResolution scaledresolution = new ScaledResolution(this.theGame.gameSettings, this.theGame.displayWidth, this.theGame.displayHeight);
        this.width = scaledresolution.getScaledWidth();
        this.height = scaledresolution.getScaledHeight();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double)this.width, (double)this.height, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
    }

    /**
     * Updates the small achievement tooltip window, showing a queued achievement if is needed.
     */
    public void updateGui()
    {
        if (this.unlockedTime != 0L)
        {
            double d0 = (double)(Minecraft.getSystemTime() - this.unlockedTime) / 3000.0D;

            if ((d0 < 0.0D || d0 > 1.0D) || hatNameText.equalsIgnoreCase(""))
            {
                this.unlockedTime = 0L;
                showNextHatUnlocked();
            }
            else
            {
                this.updateWindowScale();
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                double d1 = d0 * 2.0D;

                if (d1 > 1.0D)
                {
                    d1 = 2.0D - d1;
                }

                d1 *= 4.0D;
                d1 = 1.0D - d1;

                if (d1 < 0.0D)
                {
                    d1 = 0.0D;
                }

                d1 *= d1;
                d1 *= d1;
                int i = this.width - 160;
                int j = 0 - (int)(d1 * 36.0D);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                this.theGame.renderEngine.bindTexture("/achievement/bg.png");
                GL11.glDisable(GL11.GL_LIGHTING);
                this.drawTexturedModalRect(i, j, 96, 202, 160, 32);

                this.theGame.fontRenderer.drawString(this.headerText, i + 30, j + 7, -256);
                this.theGame.fontRenderer.drawString(this.hatNameText, i + 30, j + 18, -1);

                RenderHelper.enableGUIStandardItemLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_COLOR_MATERIAL);
                GL11.glEnable(GL11.GL_LIGHTING);
                
                //TODO render hat
                String hatNameLowCase = hatNameText.toLowerCase();
                ModelHat model = ClientProxy.models.get(hatNameLowCase);
                if(model != null)
                {
			        GL11.glPushMatrix();
			        
		            GL11.glEnable(GL11.GL_BLEND);
		            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		            
			        BufferedImage image = ClientProxy.bufferedImages.get(hatNameLowCase);

			        if (image != null)
			        {
			            if (ClientProxy.bufferedImageID.get(image) == -1)
			            {
			            	ClientProxy.bufferedImageID.put(image, Minecraft.getMinecraft().renderEngine.allocateAndSetupTexture(image));
			            }
	
			            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ClientProxy.bufferedImageID.get(image));
			            Minecraft.getMinecraft().renderEngine.resetBoundTexture();
			        }

			        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        
		            GL11.glTranslatef((float)i + 16, (float)j + 26, 50F);
		            
		            GL11.glDisable(GL11.GL_CULL_FACE);
	                GL11.glDepthMask(true);
	                GL11.glEnable(GL11.GL_DEPTH_TEST);

		            GL11.glScalef(1.0F, 1.0F, -1.0F);
		            GL11.glScalef(20.0F, 20.0F, 20.0F);
		            GL11.glRotatef(10.0F, 1.0F, 0.0F, 0.0F);
		            GL11.glRotatef(-22.5F, 0.0F, 1.0F, 0.0F);
		            GL11.glRotatef((float)(Minecraft.getSystemTime() - this.unlockedTime) / 6F, 0.0F, 1.0F, 0.0F);
		            
			        model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		            
			        GL11.glDisable(GL11.GL_BLEND);
			        
			        GL11.glPopMatrix();

                }
		    	else if(!HatHandler.reloadingHats)
		    	{
		    		if(!Hats.proxy.tickHandlerClient.requestedHats.contains(hatNameLowCase))
		    		{
		    			HatHandler.requestHat(hatNameLowCase, null);
		    			Hats.proxy.tickHandlerClient.requestedHats.add(hatNameLowCase);
		    		}
		    	}

                GL11.glDisable(GL11.GL_LIGHTING);
	            
            }
        }
    }
}
