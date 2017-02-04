package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.hats.client.gui.GuiTradeWindow;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.trade.TradeInfo;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PacketTradeOffers extends AbstractPacket
{
    public TreeMap<String, Integer> tradeHats;
    public ArrayList<ItemStack> tradeItems;

    public PacketTradeOffers(){}

    public PacketTradeOffers(TreeMap<String, Integer> hats, ArrayList<ItemStack> items)
    {
        tradeHats = hats;
        tradeItems = items;
    }

    @Override
    public void writeTo(ByteBuf buffer)
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
    public void readFrom(ByteBuf buffer)
    {
        tradeHats = new TreeMap<String, Integer>();
        tradeItems = new ArrayList<ItemStack>();

        int hatCount = buffer.readInt();

        for(int i = 0; i < hatCount; i++)
        {
            tradeHats.put(ByteBufUtils.readUTF8String(buffer), buffer.readInt());
        }

        int itemCount = buffer.readInt();

        for(int i = 0; i < itemCount; i++)
        {
            ItemStack is = new ItemStack(ByteBufUtils.readTag(buffer));

            if(!is.isEmpty())
            {
                tradeItems.add(is);
            }
        }
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
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
        return null;
    }

    @Override
    public Side receivingSide() {
        return null;
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
                    trade.theirScrollProg = MathHelper.clamp(trade.theirScrollProg * (trade.theirItemsForTrade.size() > tradeSize ? ((currentBoxes) / (currentBoxes + 1)) : ((currentBoxes) / (currentBoxes - 1))), 0.0F, 1.0F);
                }
            }
            else if(hatsSize != trade.theirHatsForTrade.size() && (trade.theirHatsForTrade.size() % 3 == 1 && hatsSize % 3 == 0 || trade.theirHatsForTrade.size() % 3 == 0 && hatsSize % 3 == 1))
            {
                float currentBoxes = (float)Math.ceil((float)Math.max(hatsSize, 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(trade.theirItemsForTrade.size(), 6) / 6F) - 3;
                if(currentBoxes > 0)
                {
                    trade.theirScrollProg = MathHelper.clamp(trade.theirScrollProg * (trade.theirHatsForTrade.size() > hatsSize ? ((currentBoxes) / (currentBoxes + 2)) : ((currentBoxes) / (currentBoxes - 2))), 0.0F, 1.0F);
                }
            }
        }
    }
}
