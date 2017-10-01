package me.ichun.mods.hats.common.trade;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.PacketString;
import me.ichun.mods.hats.common.packet.PacketTradeOffers;
import me.ichun.mods.hats.common.packet.PacketTradeReadyInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TradeInfo
{

    public final EntityPlayer trader1;
    public final EntityPlayer trader2;

    public boolean ready1;
    public boolean ready2;

    public boolean trade1;
    public boolean trade2;

    public TreeMap<String, Integer> trader1Hats = new TreeMap<>();
    public ArrayList<ItemStack> trader1Items = new ArrayList<>();

    public TreeMap<String, Integer> trader2Hats = new TreeMap<>();
    public ArrayList<ItemStack> trader2Items = new ArrayList<>();

    public boolean terminate;

    public TradeInfo(EntityPlayer t1, EntityPlayer t2)
    {
        trader1 = t1;
        trader2 = t2;
    }

    public TradeInfo initialize()
    {
        Hats.channel.sendTo(new PacketString(3, trader1.getName()), trader2);
        Hats.channel.sendTo(new PacketString(3, trader2.getName()), trader1);
        return this;
    }

    public void update()
    {
        boolean trader1Alive = trader1.isEntityAlive();
        boolean trader2Alive = trader2.isEntityAlive();
        boolean distanceCheck = trader1.getDistance(trader2) < 16D;
        boolean clearSpaceCheck = trader1.canEntityBeSeen(trader2);
        boolean sameDimension = trader1.dimension == trader2.dimension;
        if(!(trader1Alive && trader2Alive && distanceCheck && clearSpaceCheck && sameDimension))
        {
            terminate(((!trader1Alive || !trader2Alive) ? 0 : (!distanceCheck ? 1 : (!clearSpaceCheck ? 2 : 4))), !trader1Alive ? trader1 : !trader2Alive ? trader2 : null);
        }
    }

    public void terminate(int reason, EntityPlayer terminator)
    {
        terminate = true;
        //Reasons
        //0 = someone died
        //1 = distance too far
        //2 = something blocking
        //3 = terminated by player
        //4 = different dimensions
        switch(reason)
        {
            case 0:
            {
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate1", I18n.translateToLocal("hats.trade.you")), terminator);
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate1", terminator.getName()), getOtherPlayer(terminator));
                break;
            }
            case 1:
            {
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate2", trader2.getName()), trader1);
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate2", trader1.getName()), trader2);
                break;
            }
            case 2:
            {
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocal("hats.trade.terminate3"), trader1, trader2);
                break;
            }
            case 3:
            {
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate4", terminator.getName()), getOtherPlayer(terminator));
                break;
            }
            case 4:
            {
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate5", trader2.getName()), trader1);
                sendTradeMessage(I18n.translateToLocal("hats.trade.terminatePrefix") + " " + I18n.translateToLocalFormatted("hats.trade.terminate5", trader1.getName()), trader2);
                break;
            }
        }
    }

    public void sendTradeMessage(String message, EntityPlayer...players)
    {
        for(EntityPlayer player : players)
        {
            Hats.channel.sendTo(new PacketString(2, message), player);
        }
    }

    public void sendReadyInfo()
    {
        Hats.channel.sendTo(new PacketTradeReadyInfo(trader1.getName(), ready1, trader2.getName(), ready2), trader1);
        Hats.channel.sendTo(new PacketTradeReadyInfo(trader1.getName(), ready1, trader2.getName(), ready2), trader2);
    }

    public EntityPlayer getOtherPlayer(EntityPlayer player)
    {
        return player == trader1 ? trader2 : trader1;
    }

    public boolean isPlayerInTrade(EntityPlayer player)
    {
        return trader1 == player || trader2 == player;
    }

    public void receiveTradeInfo(TreeMap<String, Integer> hats, ArrayList<ItemStack> items, EntityPlayerMP player)
    {
        Hats.channel.sendTo(new PacketTradeOffers(hats, items), getOtherPlayer(player));

        EntityPlayer player1;
        EntityPlayer player2;

        TreeMap<String, Integer> oldHats;
        ArrayList<ItemStack> oldItems;

        TreeMap<String, Integer> newHats = new TreeMap<>(hats);
        ArrayList<ItemStack> newItems = new ArrayList<>(items);

        if(player == trader1)
        {
            player1 = trader1;
            player2 = trader2;

            oldHats = new TreeMap<>(trader1Hats);
            oldItems = new ArrayList<>(trader1Items);

            trader1Hats = hats;
            trader1Items = items;
        }
        else
        {
            player1 = trader2;
            player2 = trader1;

            oldHats = new TreeMap<>(trader2Hats);
            oldItems = new ArrayList<>(trader2Items);

            trader2Hats = hats;
            trader2Items = items;
        }

        Iterator<Map.Entry<String, Integer>> ite = newHats.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry<String, Integer> e = ite.next();
            for(Map.Entry<String, Integer> e1 : oldHats.entrySet())
            {
                if(e.getKey().equals(e1.getKey()))
                {
                    e.setValue(e.getValue() - e1.getValue());
                    if(e.getValue() <= 0)
                    {
                        ite.remove();
                        break;
                    }
                }
            }
        }

        for(ItemStack is : oldItems)
        {
            for(int i = newItems.size() - 1; i >= 0; i--)
            {
                if(ItemStack.areItemStacksEqual(newItems.get(i), is))
                {
                    newItems.remove(i);
                    break;
                }
            }
        }

        Iterator<Map.Entry<String, Integer>> ite1 = oldHats.entrySet().iterator();
        while(ite1.hasNext())
        {
            Map.Entry<String, Integer> e = ite1.next();
            for(Map.Entry<String, Integer> e1 : hats.entrySet())
            {
                if(e.getKey().equals(e1.getKey()))
                {
                    e.setValue(e.getValue() - e1.getValue());
                    if(e.getValue() <= 0)
                    {
                        ite1.remove();
                        break;
                    }
                }
            }
        }

        for(ItemStack is : items)
        {
            for(int i = oldItems.size() - 1; i >= 0; i--)
            {
                if(ItemStack.areItemStacksEqual(oldItems.get(i), is))
                {
                    oldItems.remove(i);
                    break;
                }
            }
        }
        //if oldstuff has it, it was removed
        //if newstuff has it, it was added

        for(int i = oldItems.size() - 1; i >= 0; i--)
        {
            for(int j = newItems.size() - 1; j >= 0; j--)
            {
                ItemStack oldStack = oldItems.get(i);
                ItemStack newStack = newItems.get(j);

                if(oldStack.isItemEqual(newStack) && ItemStack.areItemStackTagsEqual(oldStack, newStack))
                {
                    int difference = oldStack.getCount() - newStack.getCount(); //negative means you added more stuff.
                    oldItems.remove(i);
                    newItems.remove(j);
                    if(difference < 0)
                    {
                        ItemStack is = newStack.copy();
                        is.setCount(Math.abs(difference));
                        newItems.add(j, is);
                    }
                    else
                    {
                        ItemStack is = oldStack.copy();
                        is.setCount(Math.abs(difference));
                        oldItems.add(i, is);
                    }
                }
            }
        }

        for(Map.Entry<String, Integer> e : oldHats.entrySet())
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.you") + " " + I18n.translateToLocal("hats.trade.removed") + " " + TextFormatting.WHITE.toString() + e.getKey(), player1);
            sendTradeMessage(player1.getName() + " " + I18n.translateToLocal("hats.trade.removed") + " " + TextFormatting.WHITE.toString() + e.getKey(), player2);
        }
        for(ItemStack is : oldItems)
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.you") + " " + I18n.translateToLocal("hats.trade.removed") + " " + TextFormatting.WHITE.toString() + is.getCount() + " " + is.getDisplayName(), player1);
            sendTradeMessage(player1.getName() + " " + I18n.translateToLocal("hats.trade.removed") + " " + TextFormatting.WHITE.toString() + is.getCount() + " " + is.getDisplayName(), player2);
        }

        for(Map.Entry<String, Integer> e : newHats.entrySet())
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.you") + " " + I18n.translateToLocal("hats.trade.added") + " " + TextFormatting.WHITE.toString() + e.getKey(), player1);
            sendTradeMessage(player1.getName() + " " + I18n.translateToLocal("hats.trade.added") + " " + TextFormatting.WHITE.toString() + e.getKey(), player2);
        }
        for(ItemStack is : newItems)
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.you") + " " + I18n.translateToLocal("hats.trade.added") + " " + TextFormatting.WHITE.toString() + is.getCount() + " " + is.getDisplayName(), player1);
            sendTradeMessage(player1.getName() + " " + I18n.translateToLocal("hats.trade.added") + " " + TextFormatting.WHITE.toString() + is.getCount() + " " + is.getDisplayName(), player2);
        }
        resetReady();
    }

    public void resetReady()
    {
        toggleReadyTrader1(false);
        toggleReadyTrader2(false);
    }

    public void toggleReadyTrader1(boolean shouldReady)
    {
        if(!shouldReady && ready1)
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.notReady"), trader1);
            sendTradeMessage(I18n.translateToLocalFormatted("hats.trade.notReadyThem", trader1.getName()), trader2);
            ready1 = false;
            trade1 = trade2 = false;
            sendReadyInfo();
        }
        else if(shouldReady && !ready1)
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.ready"), trader1);
            sendTradeMessage(I18n.translateToLocalFormatted("hats.trade.readyThem", trader1.getName()), trader2);
            ready1 = true;
            sendReadyInfo();
        }
    }

    public void toggleReadyTrader2(boolean shouldReady)
    {
        if(!shouldReady && ready2)
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.notReady"), trader2);
            sendTradeMessage(I18n.translateToLocalFormatted("hats.trade.notReadyThem", trader2.getName()), trader1);
            ready2 = false;
            trade1 = trade2 = false;
            sendReadyInfo();
        }
        else if(shouldReady && !ready2)
        {
            sendTradeMessage(I18n.translateToLocal("hats.trade.ready"), trader2);
            sendTradeMessage(I18n.translateToLocalFormatted("hats.trade.readyThem", trader2.getName()), trader1);
            ready2 = true;
            sendReadyInfo();
        }
    }
}
