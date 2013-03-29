package hats.client.gui;

import java.util.List;

import hats.common.Hats;
import hats.common.entity.EntityHat;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;

public class GuiHatSelection extends GuiScreen 
{

	public EntityPlayer player;
	public EntityHat hat;
	public List availableHats;
	
	protected int xSize = 176;
	protected int ySize = 170;
	
	public float mouseX;
	public float mouseY;
	
	protected int guiLeft;
	protected int guiTop;
	
	public GuiHatSelection(EntityPlayer ply)
	{
		player = ply;
		hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
		availableHats = ImmutableList.copyOf(Hats.proxy.tickHandlerClient.availableHats);
	}
	
	@Override
	public void initGui()
	{
		buttonList.clear();
		
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
	}
	
    @Override
    protected void keyTyped(char c, int i)
    {
    	super.keyTyped(c, i);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
    	drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture("/mods/hats/textures/gui/hatchooser.png");
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        
    	super.drawScreen(par1, par2, par3);
    	
        this.mouseX = (float)par1;
        this.mouseY = (float)par2;

        drawPlayerOnGui(k + 42, l + 155, 55, (float)(k + 42) - (float)mouseX, (float)(l + 155 - 92) - (float)mouseY);

    }

    
    public void drawPlayerOnGui(int par1, int par2, int par3, float par4, float par5)
    {
    	if(player != null)
    	{
	    	hat = Hats.proxy.tickHandlerClient.hats.get(player.username);
	    	if(hat == null)
	    	{
	    		return;
	    	}
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        GL11.glTranslatef((float)par1, (float)par2, 50.0F);
	        GL11.glScalef((float)(-par3), (float)par3, (float)par3);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        float f2 = player.renderYawOffset;
	        float f3 = player.rotationYaw;
	        float f4 = player.rotationPitch;
	        
	        float ff3 = hat.rotationYaw;
	        float ff4 = hat.rotationPitch;
	        
	        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
	        RenderHelper.enableStandardItemLighting();
	        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
	        
	        player.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
	        player.rotationYaw = hat.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
	        player.rotationPitch = hat.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
	        player.rotationYawHead = player.rotationYaw;
	        GL11.glTranslatef(0.0F, player.yOffset, 0.0F);
	        
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        GL11.glTranslatef(0.0F, -0.22F, 0.0F);
	        RenderManager.instance.renderEntityWithPosYaw(hat, 0.0D, 0.0D, 0.0D, hat.rotationYaw, hat.rotationPitch);
	        
	        player.renderYawOffset = f2;
	        player.rotationYaw = f3;
	        player.rotationPitch = f4;
	        
	        hat.rotationYaw = ff3;
	        hat.rotationPitch = ff4;
	        
	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	}
    }

}
