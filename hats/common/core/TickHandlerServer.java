package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet131MapData;
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
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
        if (type.equals(EnumSet.of(TickType.WORLD)))
        {
        	worldTick((WorldServer)tickData[0]);
        }
        else if (type.equals(EnumSet.of(TickType.PLAYER)))
        {
        	playerTick((WorldServer)((EntityPlayer)tickData[0]).worldObj, (EntityPlayerMP)tickData[0]);
        }
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerServerHats";
	}

	public void worldTick(WorldServer world)
	{
		Iterator<Entry<EntityLiving, String>> iterator1 = mobHats.entrySet().iterator();
		
		while(iterator1.hasNext())
		{
			Entry<EntityLiving, String> e = iterator1.next();
			if(!e.getKey().isEntityAlive() || e.getKey().isChild())
			{
				iterator1.remove();
			}
		}
		
		for(int i = 0; i < world.loadedEntityList.size(); i++)
		{
			Entity ent = (Entity)world.loadedEntityList.get(i);
			if(!(ent instanceof EntityLiving) || !HatHandler.canMobHat((EntityLiving)ent))
			{
				continue;
			}
			
			EntityLiving living = (EntityLiving)ent;
			
			String hat = mobHats.get(living);
			if(hat == null)
			{
				HatInfo hatInfo = living.getRNG().nextFloat() < ((float)Hats.randomMobHat / 100F) ? HatHandler.getRandomHat() : new HatInfo();
				mobHats.put(living, hatInfo.hatName);
			}
		}
	}
	
	public void playerTick(WorldServer world, EntityPlayerMP player)
	{
	}
	
	public void playerKilledEntity(EntityLiving living, EntityPlayer player)
	{
		String hat = mobHats.get(living);
		if(hat != null)
		{
			ArrayList<String> hats = playerHats.get(player.username);
			if(!hats.contains(hat))
			{
				Iterator<Entry<File, String>> ite = HatHandler.hatNames.entrySet().iterator();
				while(ite.hasNext())
				{
					Entry<File, String> e = ite.next();
					String name = e.getKey().getName().substring(0, e.getKey().getName().length() - 4);
					if(name.equalsIgnoreCase(hat))
					{
						hats.add(name);
						
						StringBuilder sb = new StringBuilder();
						for(int i = 0; i < hats.size(); i++)
						{
							sb.append(hats.get(i));
							if(i < hats.size() - 1)
							{
								sb.append(":");
							}
						}
						
						Hats.proxy.saveData.setString(player.username + "_unlocked", sb.toString());
						Hats.proxy.saveData(DimensionManager.getWorld(0));
						
				        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				        DataOutputStream stream1 = new DataOutputStream(bytes);

				        try
				        {
				        	stream1.writeUTF(name);
				        	
				        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)2, bytes.toByteArray()), (Player)player);
				        }
				        catch(IOException e1)
				        {}
						
						break;
					}
				}
			}
		}
	}
	
	public HashMap<EntityLiving, String> mobHats = new HashMap<EntityLiving, String>();
	public HashMap<String, ArrayList<String>> playerHats = new HashMap<String, ArrayList<String>>();
}
