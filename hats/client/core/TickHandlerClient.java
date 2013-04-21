package hats.client.core;

import hats.client.gui.GuiHatSelection;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

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
		if(Hats.enableInServersWithoutMod == 1 && !serverHasMod || serverHasMod)
		{
			for(int i = 0; i < world.playerEntities.size(); i++)
			{
				EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
				if(!serverHasMod && Hats.shouldOtherPlayersHaveHats == 0 && player != Minecraft.getMinecraft().thePlayer || !player.isEntityAlive())
				{
					continue;
				}
				
				EntityHat hat = hats.get(player.username);
				if(hat == null || hat.isDead)
				{
					if(player.username.equalsIgnoreCase(mc.thePlayer.username))
					{
						//Assume respawn
						for(Entry<String, EntityHat> e : hats.entrySet())
						{
							e.getValue().setDead();
						}
					}
					
					HatInfo hatInfo = (serverHasMod ? getPlayerHat(player.username) : ((Hats.randomHat == 1 || Hats.randomHat == 2 && player != mc.thePlayer) ? HatHandler.getRandomHat() : Hats.favouriteHatInfo));
					hat = new EntityHat(world, player, hatInfo);
					hats.put(player.username, hat);
					world.spawnEntityInWorld(hat);
				}
			}
		}
		
		Iterator<Entry<String, EntityHat>> ite = hats.entrySet().iterator();
		
		while(ite.hasNext())
		{
			Entry<String, EntityHat> e = ite.next();
			if(e.getValue().worldObj.provider.dimensionId != world.provider.dimensionId || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
			{
				e.getValue().setDead();
				ite.remove();
			}
		}
		
		if(mc.currentScreen == null && !hasScreen)
		{
			if(!guiKeyDown && isPressed(Hats.guiKeyBind))
			{
				if(Hats.playerHatsMode == 3)
				{
			        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			        DataOutputStream stream = new DataOutputStream(bytes);

			        try
			        {
			        	stream.writeByte(0);
			        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)2, bytes.toByteArray()));
			        }
			        catch(IOException e)
			        {}
				}
				else
				{
					Hats.proxy.openHatsGui();
				}
			}
		}
		
		hasScreen = mc.currentScreen != null;
		
		guiKeyDown = isPressed(Hats.guiKeyBind);
	}
	
    public static boolean isPressed(int key)
    {
    	if(key < 0)
    	{
    		return Mouse.isButtonDown(key + 100);
    	}
    	return Keyboard.isKeyDown(key);
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
				hat.prevPosY = player.prevPosY + player.getEyeHeight() - 0.35F;
				hat.prevPosZ = player.prevPosZ;
				
				hat.posX = player.posX;
				hat.posY = player.posY + player.getEyeHeight() - 0.35F;
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
	
	public HatInfo getPlayerHat(String s)
	{
		HatInfo name = playerWornHats.get(s);
		if(name == null)
		{
			return new HatInfo();
		}
		return name;
	}
	
	public HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
	public HashMap<String, EntityHat> hats = new HashMap<String, EntityHat>();
	
	public ArrayList<String> availableHats = new ArrayList<String>();
	public ArrayList<String> requestedHats = new ArrayList<String>();
	
	public int serverHatMode;
	public String serverHat;
	
	public boolean serverHasMod = false;
	
	public boolean guiKeyDown;
	public boolean hasScreen;
}
