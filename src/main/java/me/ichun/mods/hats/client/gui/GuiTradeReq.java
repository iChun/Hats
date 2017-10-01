package me.ichun.mods.hats.client.gui;

import me.ichun.mods.hats.common.Hats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class GuiTradeReq extends Gui
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

    public ArrayList<String> hatList = new ArrayList<>();

    public GuiTradeReq(Minecraft par1Minecraft)
    {
        this.theGame = par1Minecraft;
        this.headerText = "\u00A7e" + I18n.translateToLocal("hats.trade.newTradeRequest");
        hatNameText = "";
    }

    public void queueHatUnlocked(String hat)
    {
        if(!hatList.contains(hat))
        {
            hatList.add(hat);
        }
        showNextHatUnlocked();
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
        GlStateManager.viewport(0, 0, this.theGame.displayWidth, this.theGame.displayHeight);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        this.width = this.theGame.displayWidth;
        this.height = this.theGame.displayHeight;
        ScaledResolution scaledresolution = new ScaledResolution(this.theGame);
        this.width = scaledresolution.getScaledWidth();
        this.height = scaledresolution.getScaledHeight();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double)this.width, (double)this.height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    /**
     * Updates the small achievement tooltip window, showing a queued achievement if is needed.
     */
    public void updateGui()
    {
        GlStateManager.pushMatrix();
        if (this.unlockedTime != 0L)
        {
            double d0 = (double)(Minecraft.getSystemTime() - this.unlockedTime) / 10000.0D;

            if ((d0 < 0.0D || d0 > 1.0D) || hatNameText.equalsIgnoreCase(""))
            {
                this.unlockedTime = 0L;
                showNextHatUnlocked();
            }
            else
            {
                this.updateWindowScale();
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
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
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableTexture2D();
                this.theGame.getTextureManager().bindTexture(texAchi);
                GlStateManager.disableLighting();
                this.drawTexturedModalRect(i, j, 96, 202, 160, 32);

                this.theGame.fontRenderer.drawString(this.headerText, i + 30, j + 7, -256);
                this.theGame.fontRenderer.drawString(I18n.translateToLocalFormatted("hats.trade.tradeRequestDesc", theGame.gameSettings.getKeyDisplayString(Hats.config.guiKeyBind.keyIndex)), i + 30, j + 18, -1);

                ResourceLocation rl = null;
                for(int ii = 0; ii < theGame.world.playerEntities.size(); ii++)
                {
                    AbstractClientPlayer player = (AbstractClientPlayer)theGame.world.playerEntities.get(ii);
                    if(player.getName().equalsIgnoreCase(this.hatNameText))
                    {
                        rl = player.getLocationSkin();
                    }
                }

                if(rl != null)
                {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.theGame.getTextureManager().bindTexture(rl);

                    double xOff = 6D;
                    double yOff = 6D;

                    double size = 20D;

                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    bufferbuilder.pos(i + xOff + 0, j + yOff + size, (double)this.zLevel).tex(8D / 64D, 16D / 32D).endVertex();
                    bufferbuilder.pos(i + xOff + size, j + yOff + size, (double)this.zLevel).tex(16D / 64D, 16D / 32D).endVertex();
                    bufferbuilder.pos(i + xOff + size, j + yOff + 0, (double)this.zLevel).tex(16D / 64D, 8D / 32D).endVertex();
                    bufferbuilder.pos(i + xOff + 0, j + yOff + 0, (double)this.zLevel).tex(8D / 64D, 8D / 32D).endVertex();
                    tessellator.draw();

                }

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.disableLighting();

            }
        }
        GlStateManager.popMatrix();
    }
}
