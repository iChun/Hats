package hats.client.gui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;

public class GuiHatSelection extends GuiScreen 
{

	public EntityPlayer player;
	
	public GuiHatSelection(EntityPlayer ply)
	{
		player = ply;
	}
	
	@Override
	public void initGui()
	{
		buttonList.clear();
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
        int k = 0;
        int l = 0;
        this.drawTexturedModalRect(k, l, 0, 0, 176, 176);
//        drawPlayerOnGui(k + 51, l + 75, 30, (float)(k + 51) - this.xSize_lo, (float)(l + 75 - 50) - this.ySize_lo);

    	super.drawScreen(par1, par2, par3);
    }

    
    public void drawPlayerOnGui(int par1, int par2, int par3, float par4, float par5)
    {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par1, (float)par2, 50.0F);
        GL11.glScalef((float)(-par3), (float)par3, (float)par3);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = player.renderYawOffset;
        float f3 = player.rotationYaw;
        float f4 = player.rotationPitch;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        player.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
        player.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
        player.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
        player.rotationYawHead = player.rotationYaw;
        GL11.glTranslatef(0.0F, player.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;
        RenderManager.instance.renderEntityWithPosYaw(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        player.renderYawOffset = f2;
        player.rotationYaw = f3;
        player.rotationPitch = f4;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

}
