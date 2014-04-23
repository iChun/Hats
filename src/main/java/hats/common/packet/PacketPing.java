package hats.common.packet;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.client.gui.GuiHatSelection;
import hats.client.gui.GuiTradeWindow;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.TimeActiveInfo;
import hats.common.trade.TradeInfo;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

public class PacketPing extends AbstractPacket
{
    public int pingId;
    public boolean pingFlag;

    public PacketPing(){}

    public PacketPing(int id, boolean flag)
    {
        pingId = id;
        pingFlag = flag;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(pingId);
        buffer.writeBoolean(pingFlag);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        pingId = buffer.readInt();
        pingFlag = buffer.readBoolean();

        if(side.isServer())
        {
            switch(pingId)
            {
                case 0: //Player requested to open GUI
                {
                    PacketHandler.sendToPlayer(Hats.channels, new PacketPing(0, FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().isPlayerOpped(player.getCommandSenderName().toLowerCase().trim())), player);
                    break;
                }
                case 1: //Received player activity state
                {
                    TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(player.getCommandSenderName());

                    if(info != null)
                    {
                        info.active = pingFlag;
                    }
                    break;
                }
                case 2: //Trade cancelled; Player closed GUI
                {
                    for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
                    {
                        if(ti.isPlayerInTrade(player))
                        {
                            ti.terminate(3, player);
                            break;
                        }
                    }
                    break;
                }
                case 3: //Ready state of trade
                {
                    for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
                    {
                        if(ti.isPlayerInTrade(player))
                        {
                            if(ti.trader1 == player)
                            {
                                ti.toggleReadyTrader1(pingFlag);
                            }
                            else
                            {
                                ti.toggleReadyTrader2(pingFlag);
                            }
                            break;
                        }
                    }
                    break;
                }
                case 4: //Mark trade point-of-no-return
                {
                    for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
                    {
                        if(ti.isPlayerInTrade(player))
                        {
                            if(ti.trader1 == player)
                            {
                                ti.trade1 = true;
                            }
                            else
                            {
                                ti.trade2 = true;
                            }

                            PacketHandler.sendToPlayer(Hats.channels, new PacketPing(2, false), ti.getOtherPlayer(player));
                            break;
                        }
                    }

                    break;
                }
            }
        }
        else
        {
            handleClient();
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        switch(pingId)
        {
            case 0: //Received response to open GUI
            {
                if(pingFlag)
                {
                    FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(Minecraft.getMinecraft().thePlayer));
                }
                else
                {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("hats.serverOnCommandGiverMode"));
                }
                break;
            }
            case 1: //Clear hats list post-death
            {
                HatHandler.populateHatsList("");
                break;
            }
            case 2: //Trigger point of no return
            {
                if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
                {
                    GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;
                    trade.pointOfNoReturn = true;
                }
                break;
            }
            case 3: //Trade success
            {
                if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
                {
                    FMLCommonHandler.instance().showGuiScreen(null);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentTranslation("hats.trade.success"));
                }
                break;
            }
        }
    }
}
