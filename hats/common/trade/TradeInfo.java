package hats.common.trade;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class TradeInfo 
{

	public final EntityPlayer trader1;
	public final EntityPlayer trader2;
	
	public boolean ready1;
	public boolean ready2;

	public boolean trade1;
	public boolean trade2;
	
	public ArrayList<String> trader1Hats = new ArrayList<String>();
	public ArrayList<ItemStack> trader1Items = new ArrayList<ItemStack>();
	
	public ArrayList<String> trader2Hats = new ArrayList<String>();
	public ArrayList<ItemStack> trader2Items = new ArrayList<ItemStack>();
	
	public boolean terminate;
	
	public TradeInfo(EntityPlayer t1, EntityPlayer t2)
	{
		trader1 = t1;
		trader2 = t2;
	}
	
	public TradeInfo initialize()
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream1 = new DataOutputStream(bytes);

        try
        {
        	stream1.writeUTF(trader1.username);
        	
        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)7, bytes.toByteArray()), (Player)trader2);
        }
        catch(IOException e)
        {}
        
        bytes = new ByteArrayOutputStream();
        stream1 = new DataOutputStream(bytes);

        try
        {
        	stream1.writeUTF(trader2.username);
        	
        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)7, bytes.toByteArray()), (Player)trader1);
        }
        catch(IOException e)
        {}

		return this;
	}
	
	public void update()
	{
		boolean trader1Alive = trader1.isEntityAlive();
		boolean trader2Alive = trader2.isEntityAlive();
		boolean distanceCheck = trader1.getDistanceToEntity(trader2) < 16D;
		boolean clearSpaceCheck = trader1.canEntityBeSeen(trader2);
		boolean sameDimension = trader1.dimension == trader2.dimension;
		if(!(trader1Alive && trader2Alive && distanceCheck && clearSpaceCheck && sameDimension))
		{
			terminate((!trader1Alive || !trader2Alive) ? 0 : !distanceCheck ? 1 : clearSpaceCheck ? 2 : 4, !trader1Alive ? trader1 : !trader2Alive ? trader2 : null);
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
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate1", new Object[] { StatCollector.translateToLocal("hats.trade.you") }), terminator);
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate1", new Object[] { terminator.username }), getOtherPlayer(terminator));
				break;
			}
			case 1:
			{
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate2", new Object[] { trader2.username }), trader1);
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate2", new Object[] { trader1.username }), trader2);
				break;
			}
			case 2:
			{
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocal("hats.trade.terminate3"), trader1, trader2);
				break;
			}
			case 3:
			{
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate4", new Object[] { terminator.username }), getOtherPlayer(terminator));
				break;
			}
			case 4:
			{
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate5", new Object[] { trader2.username }), trader1);
				sendTradeMessage(StatCollector.translateToLocal("hats.trade.terminatePrefix") + " " + StatCollector.translateToLocalFormatted("hats.trade.terminate5", new Object[] { trader1.username }), trader2);
				break;
			}
		}
	}
	
	public void sendTradeMessage(String message, EntityPlayer...players)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream1 = new DataOutputStream(bytes);

        try
        {
        	stream1.writeUTF(message);
        	
        	for(EntityPlayer player : players)
        	{
        		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)6, bytes.toByteArray()), (Player)player);
        	}
        }
        catch(IOException e)
        {}
	}
	
	public void sendReadyInfo()
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream1 = new DataOutputStream(bytes);

        try
        {
        	stream1.writeUTF(trader1.username);
        	stream1.writeBoolean(ready1);
        	stream1.writeUTF(trader2.username);
        	stream1.writeBoolean(ready2);
        	
       		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)9, bytes.toByteArray()), (Player)trader1);
       		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)9, bytes.toByteArray()), (Player)trader2);
        }
        catch(IOException e)
        {}
	}
	
	public EntityPlayer getOtherPlayer(EntityPlayer player)
	{
		return player == trader1 ? trader2 : trader1;
	}
	
	public boolean isPlayerInTrade(EntityPlayer player)
	{
		return trader1 == player || trader2 == player;
	}

	public void receiveTradeInfo(DataInputStream stream, byte[] data, EntityPlayerMP player) 
	{
        try
        {
        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)8, data), (Player)getOtherPlayer(player));
        	
        	ArrayList<String> hats = new ArrayList<String>();
        	ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        	
        	int hatCount = stream.readInt();
        	
        	for(int i = 0; i < hatCount; i++)
        	{
        		hats.add(stream.readUTF());
        	}
        	
        	int itemCount = stream.readInt();
        	
        	for(int i = 0; i < itemCount; i++)
        	{
        		ItemStack is = ItemStack.loadItemStackFromNBT(Hats.readNBTTagCompound(stream));
        		if(is != null)
        		{
        			items.add(is);
        		}
        	}
        	
        	EntityPlayer player1;
        	EntityPlayer player2;
        	
        	ArrayList<String> oldHats;
        	ArrayList<ItemStack> oldItems;
        	
        	ArrayList<String> newHats = new ArrayList<String>(hats);
        	ArrayList<ItemStack> newItems = new ArrayList<ItemStack>(items);
        	
        	if(player == trader1)
        	{
        		player1 = trader1;
        		player2 = trader2;
        		
        		oldHats = new ArrayList<String>(trader1Hats);
        		oldItems = new ArrayList<ItemStack>(trader1Items);
        		
        		trader1Hats = hats;
        		trader1Items = items;
        	}
        	else
        	{
        		player1 = trader2;
        		player2 = trader1;
        		
        		oldHats = new ArrayList<String>(trader2Hats);
        		oldItems = new ArrayList<ItemStack>(trader2Items);

        		trader2Hats = hats;
        		trader2Items = items;
        	}
        	
    		newHats.removeAll(oldHats);
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
    		
    		oldHats.removeAll(hats);
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
       					int difference = oldStack.stackSize - newStack.stackSize; //negative means you added more stuff.
   						oldItems.remove(i);
   						newItems.remove(j);
       					if(difference < 0)
       					{
       						ItemStack is = newStack.copy();
       						is.stackSize = Math.abs(difference);
       						newItems.add(j, is);
       					}
       					else
       					{
       						ItemStack is = oldStack.copy();
       						is.stackSize = Math.abs(difference);
       						oldItems.add(i, is);
       					}
      				}
       			}
       		}
       		
       		for(String s : oldHats)
       		{
       			sendTradeMessage(StatCollector.translateToLocal("hats.trade.you") + " " + StatCollector.translateToLocal("hats.trade.removed") + " " + EnumChatFormatting.WHITE.toString() + s, player1);
       			sendTradeMessage(player1.username + " " + StatCollector.translateToLocal("hats.trade.removed") + " " + EnumChatFormatting.WHITE.toString() + s, player2);
       		}
       		for(ItemStack is : oldItems)
       		{
       			sendTradeMessage(StatCollector.translateToLocal("hats.trade.you") + " " + StatCollector.translateToLocal("hats.trade.removed") + " " + EnumChatFormatting.WHITE.toString() + is.stackSize + " " + is.getDisplayName(), player1);
       			sendTradeMessage(player1.username + " " + StatCollector.translateToLocal("hats.trade.removed") + " " + EnumChatFormatting.WHITE.toString() + is.stackSize + " " + is.getDisplayName(), player2);
       		}
       		
       		for(String s : newHats)
       		{
       			sendTradeMessage(StatCollector.translateToLocal("hats.trade.you") + " " + StatCollector.translateToLocal("hats.trade.added") + " " + EnumChatFormatting.WHITE.toString() + s, player1);
       			sendTradeMessage(player1.username + " " + StatCollector.translateToLocal("hats.trade.added") + " " + EnumChatFormatting.WHITE.toString() + s, player2);
       		}
       		for(ItemStack is : newItems)
       		{
       			sendTradeMessage(StatCollector.translateToLocal("hats.trade.you") + " " + StatCollector.translateToLocal("hats.trade.added") + " " + EnumChatFormatting.WHITE.toString() + is.stackSize + " " + is.getDisplayName(), player1);
       			sendTradeMessage(player1.username + " " + StatCollector.translateToLocal("hats.trade.added") + " " + EnumChatFormatting.WHITE.toString() + is.stackSize + " " + is.getDisplayName(), player2);
       		}
        	resetReady();
        }
        catch(IOException e)
        {}
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
			sendTradeMessage(StatCollector.translateToLocal("hats.trade.notReady"), trader1);
			sendTradeMessage(StatCollector.translateToLocalFormatted("hats.trade.notReadyThem", new Object[] { trader1.username }), trader2);
			ready1 = false;
			trade1 = trade2 = false;
			sendReadyInfo();
		}
		else if(shouldReady && !ready1)
		{
			sendTradeMessage(StatCollector.translateToLocal("hats.trade.ready"), trader1);
			sendTradeMessage(StatCollector.translateToLocalFormatted("hats.trade.readyThem", new Object[] { trader1.username }), trader2);
			ready1 = true;
			sendReadyInfo();
		}
	}
	
	public void toggleReadyTrader2(boolean shouldReady)
	{
		if(!shouldReady && ready2)
		{
			sendTradeMessage(StatCollector.translateToLocal("hats.trade.notReady"), trader2);
			sendTradeMessage(StatCollector.translateToLocalFormatted("hats.trade.notReadyThem", new Object[] { trader2.username }), trader1);
			ready2 = false;
			trade1 = trade2 = false;
			sendReadyInfo();
		}
		else if(shouldReady && !ready2)
		{
			sendTradeMessage(StatCollector.translateToLocal("hats.trade.ready"), trader2);
			sendTradeMessage(StatCollector.translateToLocalFormatted("hats.trade.readyThem", new Object[] { trader2.username }), trader1);
			ready2 = true;
			sendReadyInfo();
		}
	}
}
