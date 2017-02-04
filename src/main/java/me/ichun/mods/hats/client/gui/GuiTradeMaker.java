package me.ichun.mods.hats.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.PacketString;

import java.util.ArrayList;
import java.util.Collections;

public class GuiTradeMaker extends GuiScreen
{

    public static final ResourceLocation texMaker = new ResourceLocation("hats", "textures/gui/trademaker.png");

    protected int xSize = 176;
    protected int ySize = 170;

    protected int guiLeft;
    protected int guiTop;

    public boolean forced;

    public final int ID_CANCEL = 100;

    private ArrayList<String> players = new ArrayList<String>();

    @Override
    public void updateScreen()
    {
        if(mc.world.getWorldTime() % 10L == 3 || forced)
        {
            forced = false;
            players.clear();
            for(int i = 0; i < mc.world.playerEntities.size(); i++)
            {
                EntityPlayer player = mc.world.playerEntities.get(i);
                if(player == mc.player)
                {
                    continue;
                }
                if(player.isEntityAlive() && !players.contains(player.getName()) && player.getDistanceToEntity(mc.player) < 16D && player.canEntityBeSeen(mc.player))
                {
                    players.add(player.getName());
                }
            }
            Collections.sort(players);

            buttonList.clear();

            for(int i = 0; i < players.size(); i++)
            {
                if(i == 12)
                {
                    break;
                }
                buttonList.add(new GuiButton(i, guiLeft + 6 + (i % 2 == 1 ? 84 : 0), guiTop + 4 + 22 * (int)Math.floor((double)i / 2D), 80, 20, players.get(i)));
            }

            buttonList.add(new GuiButton(ID_CANCEL, width / 2 - 45, guiTop + ySize - 31, 90, 20, I18n.format("gui.cancel")));
        }
    }

    @Override
    public void initGui()
    {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        forced = true;
    }

    @Override
    protected void actionPerformed(GuiButton btn)
    {
        if(btn.id != ID_CANCEL)
        {
            Hats.channel.sendToServer(new PacketString(0, players.get(btn.id)));
        }
        this.mc.displayGuiScreen((GuiScreen)null);
        this.mc.setIngameFocus();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        if(mc == null)
        {
            mc = Minecraft.getMinecraft();
            fontRendererObj = mc.fontRendererObj;
        }
        drawDefaultBackground();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texMaker);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);

        drawString(fontRendererObj, I18n.format("hats.trade.selectTrader"), this.guiLeft + 1, this.guiTop - 9, 0xffffff);

        super.drawScreen(par1, par2, par3);
    }
}
