package hats.client.core;

import hats.common.entity.EntityHat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient
	implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.RENDER)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{
				preRenderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
        if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
        	if(Minecraft.getMinecraft().theWorld != null)
        	{      		
        		worldTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld);
        	}
        }
        else if (type.equals(EnumSet.of(TickType.PLAYER)))
        {
        	playerTick((World)((EntityPlayer)tickData[0]).worldObj, (EntityPlayer)tickData[0]);
        }
        else if (type.equals(EnumSet.of(TickType.RENDER)))
        {
        	if(Minecraft.getMinecraft().theWorld != null)
        	{
        		renderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
        	}
        }
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT, TickType.PLAYER, TickType.RENDER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerClientHats";
	}

	public void worldTick(Minecraft mc, WorldClient world)
	{
		for(int i = 0; i < world.playerEntities.size(); i++)
		{
			EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
			EntityHat hat = hats.get(player.username);
			if(hat == null || hat.isDead)
			{
				hat = new EntityHat(world, player);
				hats.put(player.username, hat);
			}
		}
	}

	public void playerTick(World world, EntityPlayer player)
	{
	}
	
	public void preRenderTick(Minecraft mc, World world, float renderTick)
	{
		Iterator<Entry<String, EntityHat>> iterator = hats.entrySet().iterator();
		
		while(iterator.hasNext())
		{
			Entry<String, EntityHat> e = iterator.next();
			if(e.getValue().player != null)
			{
				EntityHat hat = e.getValue();
				EntityPlayer player = hat.player;
				
				hat.prevPosX = player.prevPosX;
				hat.prevPosY = player.prevPosY - player.getEyeHeight();
				hat.prevPosZ = player.prevPosZ;
				
				hat.posX = player.posX;
				hat.posY = player.posY - player.getEyeHeight();
				hat.posZ = player.posZ;
				
				hat.prevRotationPitch = player.prevRotationPitch;
				hat.rotationPitch = player.rotationPitch;
				
				hat.prevRotationYaw = player.prevRotationYawHead;
				hat.rotationYaw = player.rotationYawHead;
				
			}
		}
	}
	
	public void renderTick(Minecraft mc, World world, float renderTick)
	{
	}
	
	public HashMap<String, EntityHat> hats = new HashMap<String, EntityHat>();
}
