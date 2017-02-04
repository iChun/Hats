package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import me.ichun.mods.hats.client.gui.GuiHatSelection;
import me.ichun.mods.hats.client.gui.GuiTradeWindow;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.core.TimeActiveInfo;
import me.ichun.mods.hats.common.trade.TradeInfo;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;

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
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeInt(pingId);
        buffer.writeBoolean(pingFlag);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        pingId = buffer.readInt();
        pingFlag = buffer.readBoolean();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            switch(pingId)
            {
                case 0: //Player requested to open GUI
                {
                    Hats.channel.sendTo(new PacketPing(0, FMLCommonHandler.instance().getMinecraftServerInstance().canUseCommand(2, "")), player);
                    break;
                }
                case 1: //Received player activity state
                {
                    TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(player.getName());

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

                            Hats.channel.sendTo(new PacketPing(2, false), ti.getOtherPlayer(player));
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
        return null;
    }

    @Override
    public Side receivingSide() {
        return null;
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
                    FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().player, new GuiHatSelection(Minecraft.getMinecraft().player));
                }
                else
                {
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("hats.serverOnCommandGiverMode");
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
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages("hats.trade.success");
                }
                break;
            }
        }
    }
}
