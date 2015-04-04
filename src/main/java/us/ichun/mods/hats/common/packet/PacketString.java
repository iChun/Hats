package us.ichun.mods.hats.common.packet;

import us.ichun.mods.hats.client.gui.GuiHatSelection;
import us.ichun.mods.hats.client.gui.GuiHatUnlocked;
import us.ichun.mods.hats.client.gui.GuiTradeWindow;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.trade.TradeInfo;
import us.ichun.mods.hats.common.trade.TradeRequest;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.hats.client.gui.GuiHatSelection;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.trade.TradeRequest;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;

public class PacketString extends AbstractPacket
{
    public int pingId;
    public String pingString;

    public PacketString(){}

    public PacketString(int id, String str)
    {
        pingId = id;
        pingString = str;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(pingId);
        ByteBufUtils.writeUTF8String(buffer, pingString);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        pingId = buffer.readInt();
        pingString = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            switch(pingId)
            {
                case 0: //Received Trade request
                {
                    String plyr1 = pingString;

                    EntityPlayerMP plyr = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(plyr1);

                    if(plyr != null && plyr.isEntityAlive() && plyr.getDistanceToEntity(player) < 16D && plyr.canEntityBeSeen(player) && plyr.dimension == player.dimension)
                    {
                        TradeRequest tr1 = Hats.proxy.tickHandlerServer.playerTradeRequests.get(player.getCommandSenderName());

                        if(tr1 != null && tr1.traderName.equalsIgnoreCase(plyr.getCommandSenderName()))
                        {
                            Hats.proxy.tickHandlerServer.initializeTrade((EntityPlayerMP)player, plyr);
                            break;
                        }

                        TradeRequest tr = Hats.proxy.tickHandlerServer.playerTradeRequests.get(plyr.getCommandSenderName());
                        if(tr == null || !tr.traderName.equalsIgnoreCase(player.getCommandSenderName()))
                        {
                            Hats.proxy.tickHandlerServer.playerTradeRequests.put(plyr.getCommandSenderName(), new TradeRequest(player.getCommandSenderName()));

                            Hats.channel.sendToPlayer(new PacketString(1, player.getCommandSenderName()), plyr); //send player the trade req.
                        }
                    }
                    else
                    {
                        player.addChatMessage(new ChatComponentTranslation("hats.trade.cannotFindTrader", new Object[] { plyr1 }));
                    }
                    break;
                }
                case 1: //Accept trade request
                {
                    String plyr1 = pingString;

                    TradeRequest tr = Hats.proxy.tickHandlerServer.playerTradeRequests.get(player.getCommandSenderName());
                    if(tr == null)
                    {
                        player.addChatMessage(new ChatComponentTranslation("hats.trade.cannotAcceptTrade"));
                        break;
                    }

                    EntityPlayerMP plyr = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(plyr1);

                    if(plyr != null && plyr.isEntityAlive() && plyr.getDistanceToEntity(player) < 16D && plyr.canEntityBeSeen(player) && plyr.dimension == player.dimension)
                    {
                        Hats.proxy.tickHandlerServer.playerTradeRequests.remove(player.getCommandSenderName());
                        Hats.proxy.tickHandlerServer.initializeTrade((EntityPlayerMP)player, plyr);
                    }
                    else
                    {
                        player.addChatMessage(new ChatComponentTranslation("hats.trade.cannotAcceptTrade"));
                    }
                    break;
                }
                case 2: //Received in-trade chat message
                {
                    for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
                    {
                        if(ti.isPlayerInTrade(player))
                        {
                            Hats.console("[In-Trade] " + pingString, false);
                            ti.sendTradeMessage(pingString, ti.getOtherPlayer(player));
                            break;
                        }
                    }
                    break;
                }
                case 3:
                {
                    break;
                }
                case 4:
                {
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
            case 0: //Unlocked hat
            {
                if(Hats.proxy.tickHandlerClient.serverHats.get(pingString) == null)
                {
                    Hats.proxy.tickHandlerClient.serverHats.put(pingString, 1);
                    if(Hats.proxy.tickHandlerClient.guiHatUnlocked == null)
                    {
                        Hats.proxy.tickHandlerClient.guiHatUnlocked = new GuiHatUnlocked(Minecraft.getMinecraft());
                    }
                    Hats.proxy.tickHandlerClient.guiHatUnlocked.queueHatUnlocked(pingString);
                }
                else
                {
                    Hats.proxy.tickHandlerClient.serverHats.put(pingString, Hats.proxy.tickHandlerClient.serverHats.get(pingString) + 1);
                }
                break;
            }
            case 1: //Received trade request
            {
                Hats.proxy.tickHandlerClient.tradeReq = pingString;
                Hats.proxy.tickHandlerClient.tradeReqTimeout = 1200;

                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.successful_hit"), 1.0F));

                Hats.proxy.tickHandlerClient.guiNewTradeReq.queueHatUnlocked(Hats.proxy.tickHandlerClient.tradeReq);

                if(Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection)
                {
                    ((GuiHatSelection)Minecraft.getMinecraft().currentScreen).updateButtonList();
                }

                break;
            }
            case 2: //Received trade chat from server
            {
                if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
                {
                    GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;
                    trade.chatMessages.add(pingString);
                    if(trade.chatMessages.size() > 1 && trade.chatMessages.get(trade.chatMessages.size() - 2).startsWith(pingString) && (pingString.contains(StatCollector.translateToLocal("hats.trade.added")) || pingString.contains(StatCollector.translateToLocal("hats.trade.removed"))) && !pingString.contains(":"))
                    {
                        if(trade.chatMessages.get(trade.chatMessages.size() - 2).equals(pingString))
                        {
                            trade.chatMessages.remove(trade.chatMessages.size() - 1);
                            trade.chatMessages.remove(trade.chatMessages.size() - 1);
                            trade.chatMessages.add(pingString + " (2)");
                        }
                        else
                        {
                            String countStr = trade.chatMessages.get(trade.chatMessages.size() - 2).substring(pingString.length() + 2, trade.chatMessages.get(trade.chatMessages.size() - 2).length() - 1);
                            int count = 2;
                            try
                            {
                                count = Integer.parseInt(countStr);
                            }
                            catch(NumberFormatException e)
                            {
                            }
                            trade.chatMessages.remove(trade.chatMessages.size() - 1);
                            trade.chatMessages.remove(trade.chatMessages.size() - 1);
                            trade.chatMessages.add(pingString + " (" + Integer.toString(count + 1) + ")");
                        }
                    }
                }
                break;
            }
            case 3: //Begin Trade session
            {
                FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiTradeWindow(pingString));
                Hats.proxy.tickHandlerClient.tradeReq = null;
                Hats.proxy.tickHandlerClient.tradeReqTimeout = 0;
                break;
            }
            case 4:
            {
                break;
            }
        }
    }
}
