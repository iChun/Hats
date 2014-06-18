package hats.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.client.core.HatInfoClient;
import hats.client.render.HatRendererHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class GuiHatUnlocked extends Gui
{
    private static final ResourceLocation texAchi = new ResourceLocation("textures/gui/achievement/achievement_background.png");

    /** Holds the instance of the game (Minecraft) */
    private Minecraft theGame;

    /** Holds the latest width scaled to fit the game window. */
    private int width;

    /** Holds the latest height scaled to fit the game window. */
    private int height;
    private String headerText;
    private String hatNameText;

    private long unlockedTime;

    public ArrayList<String> hatList = new ArrayList<String>();

    private HatInfoClient info = null;

    public GuiHatUnlocked(Minecraft par1Minecraft)
    {
        this.theGame = par1Minecraft;
        this.headerText = "\u00A7e" + StatCollector.translateToLocal("hats.hatUnlocked");
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
            info = new HatInfoClient(hatNameText.toLowerCase(), 255, 255, 255, 255);
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
        GL11.glPushMatrix();
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
                this.theGame.getTextureManager().bindTexture(texAchi);
                GL11.glDisable(GL11.GL_LIGHTING);
                this.drawTexturedModalRect(i, j, 96, 202, 160, 32);

                this.theGame.fontRenderer.drawString(this.headerText, i + 30, j + 7, -256);
                this.theGame.fontRenderer.drawString(this.hatNameText, i + 30, j + 18, -1);

                RenderHelper.enableGUIStandardItemLighting();
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_COLOR_MATERIAL);
                GL11.glEnable(GL11.GL_LIGHTING);

                GL11.glTranslatef((float)i + 16, (float)j + 16, 50F);

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);

                GL11.glScalef(1.0F, -1.0F, 1.0F);
                GL11.glScalef(20.0F, 20.0F, 20.0F);
                GL11.glRotatef(10.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-22.5F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef((float)(Minecraft.getSystemTime() - this.unlockedTime) / 6F, 0.0F, 1.0F, 0.0F);

                HatRendererHelper.renderHat(info, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.000000000F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, true, 1.0F);

                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);

                GL11.glDisable(GL11.GL_LIGHTING);

            }
        }
        GL11.glPopMatrix();
    }
}
