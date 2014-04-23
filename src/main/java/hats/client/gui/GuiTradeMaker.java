package hats.client.gui;

import hats.common.Hats;
import hats.common.packet.PacketString;
import ichun.common.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

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
	
	public ArrayList<String> players = new ArrayList<String>();
	
	@Override
	public void updateScreen() 
	{
		if(mc.theWorld.getWorldTime() % 10L == 3 || forced)
		{
			forced = false;
			players.clear();
			for(int i = 0; i < mc.theWorld.playerEntities.size(); i++)
			{
				EntityPlayer player = (EntityPlayer)mc.theWorld.playerEntities.get(i);
				if(player == mc.thePlayer)
				{
					continue;
				}
				if(player.isEntityAlive() && !players.contains(player.getCommandSenderName()) && player.getDistanceToEntity(mc.thePlayer) < 16D && player.canEntityBeSeen(mc.thePlayer))
				{
					players.add(player.getCommandSenderName());
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
			
			buttonList.add(new GuiButton(ID_CANCEL, width / 2 - 45, guiTop + ySize - 31, 90, 20, StatCollector.translateToLocal("gui.cancel")));
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
            PacketHandler.sendToServer(Hats.channels, new PacketString(0, players.get(btn.id)));
    	}
        this.mc.displayGuiScreen((GuiScreen)null);
        this.mc.setIngameFocus();
        return;
    }
    
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
    	if(mc == null)
    	{
    		mc = Minecraft.getMinecraft();
    		fontRendererObj = mc.fontRenderer;
    	}
    	drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texMaker);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);

        drawString(fontRendererObj, StatCollector.translateToLocal("hats.trade.selectTrader"), this.guiLeft + 1, this.guiTop - 9, 0xffffff);
        
        super.drawScreen(par1, par2, par3);
    }
}
