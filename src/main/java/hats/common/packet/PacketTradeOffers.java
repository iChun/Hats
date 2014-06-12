package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hats.client.gui.GuiTradeWindow;
import hats.common.Hats;
import hats.common.trade.TradeInfo;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PacketTradeOffers extends AbstractPacket
{
    public HashMap<String, Integer> tradeHats;
    public ArrayList<ItemStack> tradeItems;

    public PacketTradeOffers(){}

    public PacketTradeOffers(HashMap<String, Integer> hats, ArrayList<ItemStack> items)
    {
        tradeHats = hats;
        tradeItems = items;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(tradeHats.size());

        for(Map.Entry<String, Integer> e : tradeHats.entrySet())
        {
            ByteBufUtils.writeUTF8String(buffer, e.getKey());
            buffer.writeInt(e.getValue());
        }

        buffer.writeInt(tradeItems.size());

        for(ItemStack is : tradeItems)
        {
            ByteBufUtils.writeTag(buffer, is.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        tradeHats = new HashMap<String, Integer>();
        tradeItems = new ArrayList<ItemStack>();

        int hatCount = buffer.readInt();

        for(int i = 0; i < hatCount; i++)
        {
            tradeHats.put(ByteBufUtils.readUTF8String(buffer), buffer.readInt());
        }

        int itemCount = buffer.readInt();

        for(int i = 0; i < itemCount; i++)
        {
            ItemStack is = ItemStack.loadItemStackFromNBT(ByteBufUtils.readTag(buffer));
            if(is != null)
            {
                tradeItems.add(is);
            }
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isServer())
        {
            for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
            {
                if(ti.isPlayerInTrade(player))
                {
                    ti.receiveTradeInfo(tradeHats, tradeItems, (EntityPlayerMP)player);

                    break;
                }
            }
        }
        else
        {
            handleClient(side, player);
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(Side side, EntityPlayer player)
    {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
        {
            GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;

            HashMap<String, Integer> oldHats = new HashMap<String, Integer>(trade.theirHatsForTrade);
            ArrayList<ItemStack> oldItems = new ArrayList<ItemStack>(trade.theirItemsForTrade);

            trade.theirHatsForTrade = tradeHats;
            trade.theirItemsForTrade = tradeItems;

            int tradeSize = oldItems.size();
            int hatsSize = oldHats.size();

            trade.theirCanScroll = trade.theirHatsForTrade.size() > 3 || trade.theirItemsForTrade.size() > 6;
            if(!trade.theirCanScroll)
            {
                trade.theirScrollProg = 0.0F;
            }
            else if(tradeSize != trade.theirItemsForTrade.size() && (trade.theirItemsForTrade.size() % 6 == 1 && tradeSize % 6 == 0 || trade.theirItemsForTrade.size() % 6 == 0 && tradeSize % 6 == 1))
            {
                float currentBoxes = (float)Math.ceil((float)Math.max(trade.theirHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(tradeSize, 6) / 6F) - 3;
                if(currentBoxes > 0)
                {
                    trade.theirScrollProg = MathHelper.clamp_float(trade.theirScrollProg * (trade.theirItemsForTrade.size() > tradeSize ? ((currentBoxes) / (currentBoxes + 1)) : ((currentBoxes) / (currentBoxes - 1))), 0.0F, 1.0F);
                }
            }
            else if(hatsSize != trade.theirHatsForTrade.size() && (trade.theirHatsForTrade.size() % 3 == 1 && hatsSize % 3 == 0 || trade.theirHatsForTrade.size() % 3 == 0 && hatsSize % 3 == 1))
            {
                float currentBoxes = (float)Math.ceil((float)Math.max(hatsSize, 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(trade.theirItemsForTrade.size(), 6) / 6F) - 3;
                if(currentBoxes > 0)
                {
                    trade.theirScrollProg = MathHelper.clamp_float(trade.theirScrollProg * (trade.theirHatsForTrade.size() > hatsSize ? ((currentBoxes) / (currentBoxes + 2)) : ((currentBoxes) / (currentBoxes - 2))), 0.0F, 1.0F);
                }
            }
        }
    }
}
