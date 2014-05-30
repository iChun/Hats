package hats.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import hats.common.Hats;
import hats.common.packet.PacketKingOfTheHatInfo;
import hats.common.packet.PacketPing;
import hats.common.trade.TradeInfo;
import hats.common.trade.TradeRequest;
import ichun.common.core.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.*;
import java.util.Map.Entry;

public class TickHandlerServer
{
    @SubscribeEvent
	public void serverTick(TickEvent.ServerTickEvent event)
	{
        if(event.phase == TickEvent.Phase.START)
        {
            Iterator<Entry<EntityLivingBase, String>> iterator1 = mobHats.entrySet().iterator();

            while(iterator1.hasNext())
            {
                Entry<EntityLivingBase, String> e = iterator1.next();
                if(e.getKey().isDead)
                {
                    iterator1.remove();
                }
            }

            for(Map.Entry<String, TimeActiveInfo> e : playerActivity.entrySet())
            {
                TimeActiveInfo info = e.getValue();
                info.tick();

                if(info.timeLeft == 0 && info.active)
                {
                    info.levels++;
                    info.timeLeft = Hats.config.getInt("startTime");

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
                        info.timeLeft *= 1F + (float)Hats.config.getInt("timeIncrement") / 10000F;
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
                    ArrayList<String> trader1Hats = Hats.proxy.tickHandlerServer.playerHats.get(ti.trader1.getCommandSenderName());
                    if(trader1Hats == null)
                    {
                        trader1Hats = new ArrayList<String>();
                        Hats.proxy.tickHandlerServer.playerHats.put(ti.trader1.getCommandSenderName(), trader1Hats);
                    }

                    ArrayList<String> trader2Hats = Hats.proxy.tickHandlerServer.playerHats.get(ti.trader2.getCommandSenderName());
                    if(trader2Hats == null)
                    {
                        trader2Hats = new ArrayList<String>();
                        Hats.proxy.tickHandlerServer.playerHats.put(ti.trader2.getCommandSenderName(), trader2Hats);
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

                    NBTTagCompound persistentTag = ti.trader1.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    persistentTag.setString("Hats_unlocked", sb.toString());
                    ti.trader1.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

                    StringBuilder sb1 = new StringBuilder();
                    for(int ii = 0; ii < trader2Hats.size(); ii++)
                    {
                        sb1.append(trader2Hats.get(ii));
                        if(ii < trader2Hats.size() - 1)
                        {
                            sb1.append(":");
                        }
                    }

                    persistentTag = ti.trader2.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    persistentTag.setString("Hats_unlocked", sb1.toString());
                    ti.trader2.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

                    EventHandler.sendPlayerSessionInfo(ti.trader1);
                    EventHandler.sendPlayerSessionInfo(ti.trader2);

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

                    PacketHandler.sendToPlayer(Hats.channels, new PacketPing(3, false), ti.trader1);
                    PacketHandler.sendToPlayer(Hats.channels, new PacketPing(3, false), ti.trader2);

                    ti.terminate = true;

                    activeTrades.remove(i);
                }
                else if(ti.terminate)
                {
                    activeTrades.remove(i);
                }
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
				origin.inventory.markDirty();
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
        NBTTagCompound persistentTag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persistentTag.setString("Hats_unlocked", "");
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

		Hats.proxy.playerWornHats.put(player.getCommandSenderName(), new HatInfo());
		
        PacketHandler.sendToPlayer(Hats.channels, new PacketPing(1, false), player);

        Hats.proxy.sendPlayerListOfWornHats(player, false, false);
	}
	
	public void updateNewKing(String newKing, EntityPlayer player, boolean send)
	{
		if(!Hats.config.getSessionString("currentKing").equalsIgnoreCase("") && !Hats.config.getSessionString("currentKing").equalsIgnoreCase(newKing))
		{
			EntityPlayerMP oldKing = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(Hats.config.getSessionString("currentKing"));
			if(oldKing != null)
			{
				playerDeath(oldKing);
			}

			ArrayList<String> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(Hats.config.getSessionString("currentKing"));
			if(playerHatsList == null)
			{
				playerHatsList = new ArrayList<String>();
				Hats.proxy.tickHandlerServer.playerHats.put(Hats.config.getSessionString("currentKing"), playerHatsList);
			}

			Hats.proxy.tickHandlerServer.playerHats.put(Hats.config.getSessionString("currentKing"), null);
			
			Hats.proxy.tickHandlerServer.playerHats.put(newKing, playerHatsList);
		}
		Hats.config.updateSession("currentKing", newKing);
		if(send)
		{
            if(player != null)
            {
                if(player.getCommandSenderName().equalsIgnoreCase(Hats.config.getSessionString("currentKing")))
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

                    PacketHandler.sendToPlayer(Hats.channels, new PacketKingOfTheHatInfo(Hats.config.getSessionString("currentKing"), sb.toString()), player);
                }
                else
                {
                    PacketHandler.sendToPlayer(Hats.channels, new PacketKingOfTheHatInfo(Hats.config.getSessionString("currentKing"), ""), player);
                }
            }
            else
            {
                PacketHandler.sendToAll(Hats.channels, new PacketKingOfTheHatInfo(Hats.config.getSessionString("currentKing"), ""));
            }
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
