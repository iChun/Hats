package hats.common.core;

import hats.common.Hats;
import hats.common.trade.TradeInfo;
import hats.common.trade.TradeRequest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class TickHandlerServer implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.SERVER)))
        {
        	serverTick();
        }
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
        if (type.equals(EnumSet.of(TickType.WORLD)))
        {
        	worldTick((WorldServer)tickData[0]);
        }
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.WORLD, TickType.SERVER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerServerHats";
	}

	public void worldTick(WorldServer world)
	{
		if(world.provider.dimensionId == 0)
		{
			Iterator<Entry<EntityLivingBase, String>> iterator1 = mobHats.entrySet().iterator();
			
			while(iterator1.hasNext())
			{
				Entry<EntityLivingBase, String> e = iterator1.next();
				if(e.getKey().isDead || e.getKey().isChild())
				{
					iterator1.remove();
				}
			}
		}
		for(int i = 0; i < world.loadedEntityList.size(); i++)
		{
			Entity ent = (Entity)world.loadedEntityList.get(i);
			if(!(ent instanceof EntityLivingBase) || !HatHandler.canMobHat((EntityLivingBase)ent) || mobHats.containsKey(ent))
			{
				continue;
			}
			
			EntityLivingBase living = (EntityLivingBase)ent;
			
			String hat = mobHats.get(living);
			if(hat == null)
			{
				boolean fromSpawner = false;
				for(int k = 0; k < world.loadedTileEntityList.size(); k++)
				{
					TileEntity te = (TileEntity)world.loadedTileEntityList.get(k);
					if(!(te instanceof TileEntityMobSpawner))
					{
						continue;
					}
					
					TileEntityMobSpawner spawner = (TileEntityMobSpawner)te;
					MobSpawnerBaseLogic logic = spawner.getSpawnerLogic();
					if(logic.canRun())
					{
						Entity entity = EntityList.createEntityByName(logic.getEntityNameToSpawn(), logic.getSpawnerWorld());
						if(entity != null)
						{
							if(living.getClass() == entity.getClass())
							{
								List list = logic.getSpawnerWorld().getEntitiesWithinAABB(entity.getClass(), AxisAlignedBB.getAABBPool().getAABB((double)logic.getSpawnerX(), (double)logic.getSpawnerY(), (double)logic.getSpawnerZ(), (double)(logic.getSpawnerX() + 1), (double)(logic.getSpawnerY() + 1), (double)(logic.getSpawnerZ() + 1)).expand((double)(4 * 2), 4.0D, (double)(4 * 2)));
								if(list.contains(living))
								{
									fromSpawner = true;
									break;
								}
							}
						}
					}
				}
				HatInfo hatInfo = living.getRNG().nextFloat() < ((float)Hats.randomMobHat / 100F) && !fromSpawner ? HatHandler.getRandomHat() : new HatInfo();
				mobHats.put(living, hatInfo.hatName);
			}
		}
	}
	
	public void serverTick()
	{
		for(Map.Entry<String, TimeActiveInfo> e : playerActivity.entrySet())
		{
			TimeActiveInfo info = e.getValue();
			info.tick();
			
			if(info.timeLeft == 0 && info.active)
			{
				info.levels++;
				info.timeLeft = Hats.startTime;
				
				ArrayList<String> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(e.getKey());
				if(playerHatsList == null)
				{
					playerHatsList = new ArrayList<String>();
					Hats.proxy.tickHandlerServer.playerHats.put(e.getKey(), playerHatsList);
				}

				ArrayList<String> newHats = HatHandler.getAllHatsAsList();
				
				EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(e.getKey());
				
				if(player != null && !newHats.isEmpty())
				{
					HatHandler.unlockHat(player, newHats.get(player.worldObj.rand.nextInt(newHats.size())));
				}

				for(int i = 0; i < info.levels; i++)
				{
					info.timeLeft *= 1F + Hats.timeIncrement;
				}
			}
		}
		
		Iterator<Entry<String, TradeRequest>> ite = playerTradeRequests.entrySet().iterator();
		
		while(ite.hasNext())
		{
			Entry<String, TradeRequest> e = ite.next();
			TradeRequest tr = e.getValue();
			tr.timePending++;
			if(tr.timePending >= 1200)
			{
				ite.remove();
			}
		}
		
		for(int i = activeTrades.size() - 1; i >= 0; i--)
		{
			TradeInfo ti = activeTrades.get(i);
			ti.update();
			if(ti.trade1 && ti.trade2)
			{
				ArrayList<String> trader1Hats = Hats.proxy.tickHandlerServer.playerHats.get(ti.trader1.username);
				if(trader1Hats == null)
				{
					trader1Hats = new ArrayList<String>();
					Hats.proxy.tickHandlerServer.playerHats.put(ti.trader1.username, trader1Hats);
				}

				ArrayList<String> trader2Hats = Hats.proxy.tickHandlerServer.playerHats.get(ti.trader2.username);
				if(trader2Hats == null)
				{
					trader2Hats = new ArrayList<String>();
					Hats.proxy.tickHandlerServer.playerHats.put(ti.trader2.username, trader2Hats);
				}

				transferHat(trader1Hats, trader2Hats, ti.trader1Hats);
				transferHat(trader2Hats, trader1Hats, ti.trader2Hats);
				
				StringBuilder sb = new StringBuilder();
				for(int ii = 0; ii < trader1Hats.size(); ii++)
				{
					sb.append(trader1Hats.get(ii));
					if(ii < trader1Hats.size() - 1)
					{
						sb.append(":");
					}
				}
				
				Hats.proxy.saveData.setString(ti.trader1.username + "_unlocked", sb.toString());

				StringBuilder sb1 = new StringBuilder();
				for(int ii = 0; ii < trader2Hats.size(); ii++)
				{
					sb1.append(trader2Hats.get(ii));
					if(ii < trader2Hats.size() - 1)
					{
						sb1.append(":");
					}
				}
				
				Hats.proxy.saveData.setString(ti.trader2.username + "_unlocked", sb.toString());
				
				ConnectionHandler.sendPlayerSessionInfo(ti.trader1);
				ConnectionHandler.sendPlayerSessionInfo(ti.trader2);
				
				removeItems(ti.trader1, ti.trader1Items);
				removeItems(ti.trader2, ti.trader2Items);
				
				for(ItemStack is : ti.trader2Items)
				{
					if(!ti.trader1.inventory.addItemStackToInventory(is))
					{
						ti.trader1.dropPlayerItemWithRandomChoice(is, false);
					}
				}

				for(ItemStack is : ti.trader1Items)
				{
					if(!ti.trader2.inventory.addItemStackToInventory(is))
					{
						ti.trader2.dropPlayerItemWithRandomChoice(is, false);
					}
				}
				
		        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		        DataOutputStream stream1 = new DataOutputStream(bytes);

		        try
		        {
		        	stream1.writeByte(0);
		        	
		       		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)11, bytes.toByteArray()), (Player)ti.trader1);
		       		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)11, bytes.toByteArray()), (Player)ti.trader2);
		        }
		        catch(IOException e)
		        {}
				
		        Hats.proxy.saveData(DimensionManager.getWorld(0));
		        
				ti.terminate = true;
				
				activeTrades.remove(i);
			}
			else if(ti.terminate)
			{
				activeTrades.remove(i);
			}
		}
	}
	
	public void transferHat(ArrayList<String> origin, ArrayList<String> destination, ArrayList<String> hatsList) 
	{
		origin.removeAll(hatsList);
		destination.addAll(hatsList);
		
		Collections.sort(origin);
		Collections.sort(destination);
	}
	
	public void removeItems(EntityPlayer origin, ArrayList<ItemStack> itemsList) 
	{
		ArrayList<ItemStack> itemsListCopy = new ArrayList<ItemStack>();
		for(ItemStack is : itemsList)
		{
			itemsListCopy.add(is.copy());
		}
		
		for(int i = origin.inventory.mainInventory.length - 1; i >= 0; i--)
		{
			ItemStack is = origin.inventory.mainInventory[i];
			if(is != null)
			{
				for(int j = itemsListCopy.size() - 1; j >= 0; j--)
				{
					ItemStack is1 = itemsListCopy.get(j);
					if(is1.isItemEqual(is) && ItemStack.areItemStackTagsEqual(is, is1))
					{
						while(is.stackSize > 0 && is1.stackSize > 0)
						{
							is.stackSize--;
							is1.stackSize--;
						}
						if(is1.stackSize <= 0)
						{
							itemsListCopy.remove(j);
						}
					}
				}
				if(is.stackSize <= 0)
				{
					origin.inventory.mainInventory[i] = null;
				}
				origin.inventory.onInventoryChanged();
			}
		}
		
		for(ItemStack is : itemsListCopy)
		{
			for(int i = itemsList.size() - 1; i >= 0; i--)
			{
				ItemStack is1 = itemsList.get(i);
				if(is1.isItemEqual(is) && ItemStack.areItemStackTagsEqual(is, is1))
				{
					while(is.stackSize > 0 && is1.stackSize > 0)
					{
						is.stackSize--;
						is1.stackSize--;
					}
					if(is1.stackSize <= 0)
					{
						itemsList.remove(i);
					}
				}
			}
		}
	}

	public void playerKilledEntity(EntityLivingBase living, EntityPlayer player)
	{
		String hat = mobHats.get(living);
		if(hat != null)
		{
			HatHandler.unlockHat(player, hat);
		}
		mobHats.remove(living);
	}
	
	public void playerDeath(EntityPlayer player)
	{
		Hats.proxy.saveData.setString(player.username + "_unlocked", "");
		Hats.proxy.playerWornHats.put(player.username, new HatInfo());
		
		Hats.proxy.saveData(DimensionManager.getWorld(0));
		
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream1 = new DataOutputStream(bytes);

        try
        {
        	stream1.writeByte(0);
        	
        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)3, bytes.toByteArray()), (Player)player);
        }
        catch(IOException e1)
        {}

        Hats.proxy.sendPlayerListOfWornHats(player, false, false);
	}
	
	public void updateNewKing(String newKing, EntityPlayer player, boolean send)
	{
		if(!SessionState.currentKing.equalsIgnoreCase("") && !SessionState.currentKing.equalsIgnoreCase(newKing))
		{
			EntityPlayerMP oldKing = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(SessionState.currentKing);
			if(oldKing != null)
			{
				playerDeath(oldKing);
			}
			else if(Hats.proxy.saveData != null)
			{
				Hats.proxy.saveData.setString(SessionState.currentKing + "_unlocked", "");
			}
			
			ArrayList<String> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(SessionState.currentKing);
			if(playerHatsList == null)
			{
				playerHatsList = new ArrayList<String>();
				Hats.proxy.tickHandlerServer.playerHats.put(SessionState.currentKing, playerHatsList);
			}

			Hats.proxy.tickHandlerServer.playerHats.put(SessionState.currentKing, null);
			
			Hats.proxy.tickHandlerServer.playerHats.put(newKing, playerHatsList);
		}
		if(Hats.proxy.saveData != null && !Hats.proxy.saveData.getString("HatsKingOfTheHill_lastKing").equalsIgnoreCase(newKing))
		{
			ArrayList<String> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(newKing);
			if(playerHatsList == null)
			{
				playerHatsList = new ArrayList<String>();
				Hats.proxy.tickHandlerServer.playerHats.put(newKing, playerHatsList);
			}

			ArrayList<String> newHats = HatHandler.getAllHatsAsList();
			
			ArrayList<String> collectors = new ArrayList<String>();
			
			if(!SessionState.currentKing.equalsIgnoreCase(""))
			{
				for(String s : newHats)
				{
					if(s.startsWith("(C) ") && s.substring(4).toLowerCase().startsWith(SessionState.currentKing.toLowerCase())
							|| s.equalsIgnoreCase("(C) iChun") && SessionState.currentKing.equalsIgnoreCase("ohaiiChun") //special casing for initial contrib hats.
							|| s.equalsIgnoreCase("(C) Mr. Haz") && SessionState.currentKing.equalsIgnoreCase("damien95")
							|| s.equalsIgnoreCase("(C) Fridgeboy") && SessionState.currentKing.equalsIgnoreCase("lacsap32"))
	
					{
						collectors.add(s);
					}
				}
			}
			
			EntityPlayerMP newKingEnt = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(newKing);
			
			if(newKingEnt != null && !newHats.isEmpty())
			{
				HatHandler.unlockHat(newKingEnt, newHats.get(newKingEnt.worldObj.rand.nextInt(newHats.size())));
				for(String s : collectors)
				{
					HatHandler.unlockHat(newKingEnt, s);
				}
			}
			
			Hats.proxy.saveData.setString("HatsKingOfTheHill_lastKing", newKing);
		}
		SessionState.currentKing = newKing;
		if(send)
		{
	        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	        DataOutputStream stream1 = new DataOutputStream(bytes);
	
	        try
	        {
	        	stream1.writeByte(4);
	        	stream1.writeUTF(SessionState.currentKing);
	        	
	        	if(player != null)
	        	{
	        		if(player.username.equalsIgnoreCase(SessionState.currentKing))
	        		{
						StringBuilder sb = new StringBuilder();
	        			ArrayList<String> hats = Hats.proxy.tickHandlerServer.playerHats.get(newKing);
	        			if(hats != null)
	        			{
    						for(int i = 0; i < hats.size(); i++)
    						{
    							sb.append(hats.get(i));
    							if(i < hats.size() - 1)
    							{
    								sb.append(":");
    							}
    						}
	        			}

	        			stream1.writeUTF(sb.toString());
	        			
	        			PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
	        		}
	        		else
	        		{
	        			PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)4, bytes.toByteArray()), (Player)player);
	        		}
	        	}
	        	else
	        	{
	        		PacketDispatcher.sendPacketToAllPlayers(new Packet131MapData((short)Hats.getNetId(), (short)4, bytes.toByteArray()));
	        	}
	        }
	        catch(IOException e1)
	        {}
		}
	}
	
	public void initializeTrade(EntityPlayerMP player, EntityPlayerMP plyr) 
	{
		if(player == null || plyr == null)
		{
			return;
		}
		activeTrades.add((new TradeInfo(player, plyr)).initialize());
	}
	
	public WeakHashMap<EntityLivingBase, String> mobHats = new WeakHashMap<EntityLivingBase, String>();
	public HashMap<String, ArrayList<String>> playerHats = new HashMap<String, ArrayList<String>>();
	public HashMap<String, TimeActiveInfo> playerActivity = new HashMap<String, TimeActiveInfo>();
	
	public HashMap<String, TradeRequest> playerTradeRequests = new HashMap<String, TradeRequest>();
	
	public ArrayList<TradeInfo> activeTrades = new ArrayList<TradeInfo>();
}
