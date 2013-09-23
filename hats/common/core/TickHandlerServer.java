package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
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
		if(world.provider.dimensionId == 0)
		{
			Iterator<Entry<EntityLivingBase, String>> iterator1 = mobHats.entrySet().iterator();
			
			while(iterator1.hasNext())
			{
				Entry<EntityLivingBase, String> e = iterator1.next();
				if(!e.getKey().isEntityAlive() || e.getKey().isChild())
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
	
	public void playerTick(WorldServer world, EntityPlayerMP player)
	{
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
	
	public WeakHashMap<EntityLivingBase, String> mobHats = new WeakHashMap<EntityLivingBase, String>();
	public HashMap<String, ArrayList<String>> playerHats = new HashMap<String, ArrayList<String>>();
}
