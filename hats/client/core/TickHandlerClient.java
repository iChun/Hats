package hats.client.core;

import hats.api.RenderOnEntityHelper;
import hats.client.gui.GuiHatUnlocked;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.core.SessionState;
import hats.common.entity.EntityHat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
		if(worldInstance != world)
		{
			worldInstance = world;
			mobHats.clear();
			hats.clear();
			requestMobHats.clear();
			requestedMobHats.clear();
			requestCooldown = 40;
		}
		if(SessionState.showJoinMessage)
		{
			SessionState.showJoinMessage = false;
			//TODO update this!
			mc.thePlayer.addChatMessage(SessionState.serverHatMode == 4 ? StatCollector.translateToLocal("hats.firstJoin.hatHunting") : StatCollector.translateToLocal("hats.firstJoin.kingOfTheHat.hasKing"));
		}
		if(Hats.enableInServersWithoutMod == 1 && !SessionState.serverHasMod || SessionState.serverHasMod)
		{
			for(int i = 0; i < world.playerEntities.size(); i++)
			{
				EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
				if(!SessionState.serverHasMod && Hats.shouldOtherPlayersHaveHats == 0 && player != Minecraft.getMinecraft().thePlayer || !player.isEntityAlive())
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
						for(Entry<Integer, EntityHat> e : mobHats.entrySet())
						{
							e.getValue().setDead();
						}
						requestedMobHats.clear();
					}
					
					HatInfo hatInfo = (SessionState.serverHasMod ? getPlayerHat(player.username) : ((Hats.randomHat == 1 || Hats.randomHat == 2 && player != mc.thePlayer) ? HatHandler.getRandomHat() : Hats.favouriteHatInfo));
					hat = new EntityHat(world, player, hatInfo);
					hats.put(player.username, hat);
					world.spawnEntityInWorld(hat);
				}
			}
		}
		
		if(clock != world.getWorldTime() || !world.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
		{
//			ScaledResolution reso = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
//			System.out.println(reso.getScaledHeight());
			clock = world.getWorldTime();
			if(requestCooldown > 0)
			{
				requestCooldown--;
			}
			
			if(clock % 5L == 0L && requestCooldown <= 0)
			{
				if(requestMobHats.size() > 0)
				{
			        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			        DataOutputStream stream = new DataOutputStream(bytes);
		
			        try
			        {
						for(int i = 0 ; i < requestMobHats.size(); i++)
						{
				        	stream.writeBoolean(true); 
				        	stream.writeInt(requestMobHats.get(i)); 
				        }
						stream.writeBoolean(false);
			        	
			        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)3, bytes.toByteArray()));
					}
			        catch(IOException e)
			        {}
					requestMobHats.clear();
				}
			}
			
			if(SessionState.serverHatMode == 6)
			{
				lastHitKey++;
				
				if(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode) || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode))
				{
					lastHitKey = 0;
				}
				if(clock % 107L == 0)
				{
					if((lastHitKey > 100 || lastHitKey == 0 && posX == mc.thePlayer.posX && posY == mc.thePlayer.posY && posZ == mc.thePlayer.posZ) && (rotationYaw == mc.thePlayer.rotationYaw && rotationPitch == mc.thePlayer.rotationPitch || posX == mc.thePlayer.posX && posY == mc.thePlayer.posY && posZ == mc.thePlayer.posZ || mc.thePlayer.ridingEntity != null) || hasScreen)
					{
						if(isActive)
						{
							isActive = false;
							
							try
							{
						        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						        DataOutputStream stream = new DataOutputStream(bytes);
					
								stream.writeBoolean(false);
					        	
					        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)4, bytes.toByteArray()));
							}
							catch(IOException e)
							{
							}
						}
					}
					else
					{
						if(!isActive)
						{
							isActive = true;
							
							try
							{
						        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						        DataOutputStream stream = new DataOutputStream(bytes);
					
								stream.writeBoolean(true);
					        	
					        	PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Hats.getNetId(), (short)4, bytes.toByteArray()));
							}
							catch(IOException e)
							{
							}
						}
					}
					rotationYaw = mc.thePlayer.rotationYaw;
					rotationPitch = mc.thePlayer.rotationPitch;
					posX = mc.thePlayer.posX;
					posY = mc.thePlayer.posY;
					posZ = mc.thePlayer.posZ;
				}
			}
		}

		if(Hats.randomMobHat > 0 && !(SessionState.serverHasMod && SessionState.serverHatMode == 4) || SessionState.serverHasMod && SessionState.serverHatMode == 4)
		{
			for(int i = 0; i < world.loadedEntityList.size(); i++)
			{
				Entity ent = (Entity)world.loadedEntityList.get(i);
				if(!(ent instanceof EntityLivingBase) || !(SessionState.serverHasMod && SessionState.serverHatMode == 4) && !HatHandler.canMobHat((EntityLivingBase)ent) || ent instanceof EntityPlayer)
				{
					continue;
				}
				
				EntityLivingBase living = (EntityLivingBase)ent;
				
				EntityHat hat = mobHats.get(living.entityId);
				if(hat == null || hat.isDead)
				{
					if(!SessionState.serverHasMod || SessionState.serverHatMode != 4)
					{
						HatInfo hatInfo = living.getRNG().nextFloat() < ((float)Hats.randomMobHat / 100F) ? (Hats.randomHat >= 1 ? HatHandler.getRandomHat() : Hats.favouriteHatInfo) : new HatInfo();
						hat = new EntityHat(world, living, hatInfo);
						mobHats.put(living.entityId, hat);
						world.spawnEntityInWorld(hat);
					}
					else if(!requestMobHats.contains(living.entityId) && !requestedMobHats.contains(living.entityId))
					{
						requestMobHats.add(living.entityId);
						requestedMobHats.add(living.entityId);
					}
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
		
		Iterator<Entry<Integer, EntityHat>> ite1 = mobHats.entrySet().iterator();
		
		while(ite1.hasNext())
		{
			Entry<Integer, EntityHat> e = ite1.next();
			if(e.getValue().worldObj.provider.dimensionId != world.provider.dimensionId || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
			{
				e.getValue().setDead();
				ite1.remove();
			}
		}
		
		if(mc.currentScreen == null && !hasScreen)
		{
			if(!guiKeyDown && isPressed(Hats.guiKeyBind))
			{
				if(SessionState.serverHatMode == 3)
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
				else if(SessionState.serverHatMode == 2)
				{
					mc.thePlayer.addChatMessage(StatCollector.translateToLocal("hats.lockedMode"));
				}
				else if(SessionState.serverHatMode == 5 && !SessionState.currentKing.equalsIgnoreCase(mc.thePlayer.username))
				{
					mc.thePlayer.addChatMessage(StatCollector.translateToLocalFormatted("hats.kingOfTheHat.notKing", new Object[] { SessionState.currentKing }));
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
		currentHatRenders = 0;
		Iterator<Entry<String, EntityHat>> iterator = hats.entrySet().iterator();
		
		while(iterator.hasNext())
		{
			Entry<String, EntityHat> e = iterator.next();
			if(e.getValue().parent != null)
			{
				EntityHat hat = e.getValue();
				
				updateHatPosAndAngle(hat, hat.renderingParent);
			}
		}
		
		Iterator<Entry<Integer, EntityHat>> iterator1 = mobHats.entrySet().iterator();
		
		while(iterator1.hasNext())
		{
			Entry<Integer, EntityHat> e = iterator1.next();
			if(e.getValue().parent != null)
			{
				EntityHat hat = e.getValue();
				
				updateHatPosAndAngle(hat, hat.parent);
			}
		}
	}
	
	public void updateHatPosAndAngle(EntityHat hat, EntityLivingBase parent)
	{
		hat.lastTickPosX = hat.parent.lastTickPosX;
		hat.lastTickPosY = hat.parent.lastTickPosY;
		hat.lastTickPosZ = hat.parent.lastTickPosZ;
		
		hat.prevPosX = hat.parent.prevPosX;
		hat.prevPosY = hat.parent.prevPosY;
		hat.prevPosZ = hat.parent.prevPosZ;
		
		hat.posX = hat.parent.posX;
		hat.posY = hat.parent.posY;
		hat.posZ = hat.parent.posZ;
		
		RenderOnEntityHelper helper = HatHandler.getRenderHelper(parent.getClass());
		
		if(helper != null)
		{
			hat.prevRotationPitch = helper.getPrevRotationPitch(parent);
			hat.rotationPitch = helper.getRotationPitch(parent);
			
			hat.prevRotationYaw = helper.getPrevRotationYaw(parent);
			hat.rotationYaw = helper.getRotationYaw(parent);
		}
	}
	
	public void renderTick(Minecraft mc, World world, float renderTick)
	{
		if(guiHatUnlocked == null)
		{
			guiHatUnlocked = new GuiHatUnlocked(mc);
		}
		guiHatUnlocked.updateGui();
	}
	
	public HatInfo getPlayerHat(String s)
	{
		HatInfo name = playerWornHats.get(s);
		if(name == null)
		{
			if(SessionState.serverHatMode == 2)
			{
				return new HatInfo(SessionState.serverHat);
			}
			return new HatInfo();
		}
		return name;
	}
	
	public HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
	public HashMap<String, EntityHat> hats = new HashMap<String, EntityHat>();
	public HashMap<Integer, EntityHat> mobHats = new HashMap<Integer, EntityHat>();
	
	public HashMap<Integer, EntityHat> rendered = new HashMap<Integer, EntityHat>();
	
	public ArrayList<String> availableHats = new ArrayList<String>();
	public ArrayList<String> requestedHats = new ArrayList<String>();
	public ArrayList<String> serverHats = new ArrayList<String>();
	public ArrayList<Integer> requestMobHats = new ArrayList<Integer>();
	public ArrayList<Integer> requestedMobHats = new ArrayList<Integer>();
	
	public World worldInstance;
	
	public long clock;
	
	public long lastHitKey;
	public float rotationYaw;
	public float rotationPitch;
	
	public double posX;
	public double posY;
	public double posZ;
	
	public boolean isActive;
	
	public boolean guiKeyDown;
	public boolean hasScreen;
	public int currentHatRenders;
	public int requestCooldown;
	
	public GuiHatUnlocked guiHatUnlocked;
}
